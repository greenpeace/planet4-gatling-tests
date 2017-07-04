package base

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class Scenario2 extends Simulation {

    val conf = ConfigFactory.load()
    val baseURL = conf.getString("application.baseURL")
    val adminUser = conf.getString("application.adminUser")
    val adminPassword = conf.getString("application.adminPassword")

    println("Starting performance test at " + baseURL)

    val httpConf = http
        .baseURL(baseURL)
        .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .doNotTrackHeader("1")
        .acceptLanguageHeader("en-US,en;q=0.5")
        .acceptEncodingHeader("gzip, deflate")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/52.0")

    object Browse {
        val Home = exec(http("GET Home")
            .get("/"))
            .pause(10)

        val Page = exec(http("GET Page")
            .get("/?page_id=2"))
            .pause(10)

        val Post = exec(http("GET Post")
            .get("/?p=1"))
            .pause(10)
    }

    object Comment {

        val feeder = csv("comments.csv").random

        val Post = feed(feeder)
            .exec(http("POST wp-comments-post")
            .post("/wp-comments-post.php")
            .formParam("comment", "${commentComment}")
            .formParam("author", "${commentAuthor}")
            .formParam("email", "${commentEmail}")
            .formParam("url", "${commentUrl}")
            .formParam("submit", "Post Comment")
            .formParam("comment_post_ID", "1")
            .formParam("comment_parent", "0"))
    }

    object Auth {
        val Login = exec(http("GET wp-login")
            .get("/wp-login.php"))
            .pause(3)
            .exec(http("POST Login")
            .post("/wp-login.php")
            .formParam("log", adminUser)
            .formParam("pwd", adminPassword)
            .formParam("wp-submit", "Log In")
            .formParam("redirect_to", baseURL + "/wp-admin/")
            .formParam("testcookie", "1"))
            .pause(1)
            .exec(http("GET wp-admin")
            .get("/wp-admin")
            .check(regex("""action=logout&#038;_wpnonce=([a-z0-9]+)""").saveAs("_logout_nonce")))

        val Logout = exec(http("GET Logout")
           .get("/wp-login.php?action=logout&_wpnonce=${_logout_nonce}"))
    }

    object Edit {
        val Post = exec(http("GET post")
            .get("/wp-admin/post.php?post=1&action=edit")
            .check(regex("""name="_wpnonce" value="([a-z0-9]+)"""").saveAs("_edit_nonce")))
    		.pause(10)
    		.exec(http("POST post")
            .post("/wp-admin/post.php")
            .formParam("_wpnonce",  "${_edit_nonce}")
            .formParam("_wp_http_referer", "/wp-admin/post.php?post=1&action=edit")
            .formParam("user_ID", "1")
            .formParam("action", "editpost")
            .formParam("originalaction", "editpost")
            .formParam("post_author", "1")
            .formParam("post_type", "post")
            .formParam("original_post_status", "publish")
            .formParam("referredby", baseURL + "/wp-admin/edit.php")
            .formParam("_wp_original_http_referer", baseURL + "/wp-admin/edit.php")
            .formParam("post_ID", "1")
            .formParam("meta-box-order-nonce", "e67034ddb5")
            .formParam("closedpostboxesnonce", "cd620ccb79")
            .formParam("post_title", "Hello world!")
            .formParam("samplepermalinknonce", "efd10bea5b")
            .formParam("content", """Welcome to WordPress. This is your first post. Edit or delete it, then start writing! Edited ${_edit_nonce}.""")
            .formParam("wp-preview", "")
            .formParam("hidden_post_status", "publish")
            .formParam("post_status", "publish")
            .formParam("hidden_post_password", "")
            .formParam("hidden_post_visibility", "public")
            .formParam("visibility", "public")
            .formParam("post_password", "")
            .formParam("mm", "04")
            .formParam("jj", "12")
            .formParam("aa", "2017")
            .formParam("hh", "05")
            .formParam("mn", "54")
            .formParam("ss", "27")
            .formParam("hidden_mm", "04")
            .formParam("cur_mm", "04")
            .formParam("hidden_jj", "12")
            .formParam("cur_jj", "12")
            .formParam("hidden_aa", "2017")
            .formParam("cur_aa", "2017")
            .formParam("hidden_hh", "05")
            .formParam("cur_hh", "06")
            .formParam("hidden_mn", "54")
            .formParam("cur_mn", "40")
            .formParam("original_publish", "Update")
            .formParam("save", "Update")
            .formParam("post_category[]", "0")
            .formParam("post_category[]", "1")
            .formParam("newcategory", "New Category Name")
            .formParam("newcategory_parent", "-1")
            .formParam("_ajax_nonce-add-category", "d6b3ee78e7")
            .formParam("tax_input[post_tag]", "")
            .formParam("newtag[post_tag]", "")
            .formParam("_thumbnail_id", "-1")
            .formParam("excerpt", "")
            .formParam("trackback_url", "")
            .formParam("metakeyinput", "")
            .formParam("metavalue", "")
            .formParam("_ajax_nonce-add-meta", "1c49517088")
            .formParam("advanced_view", "1")
            .formParam("comment_status", "open")
            .formParam("ping_status", "open")
            .formParam("add_comment_nonce", "a1f603eaa0")
            .formParam("_ajax_fetch_list_nonce", "8a5c287dec")
            .formParam("_wp_http_referer", "/wp-admin/post.php?post=1&action=edit")
            .formParam("post_name", "hello-world")
            .formParam("post_author_override", "1"))
    }

    val visitors = scenario("Visitors").exec(Browse.Home, Browse.Page, Browse.Post)
    val commentators = scenario("Commentators").exec(Browse.Post, Comment.Post)
    val admins = scenario("Admins").exec(Auth.Login, Auth.Logout)

    /* setUp(commentators.inject(atOnceUsers(1)).protocols(httpConf)) */
    setUp(
      visitors.inject(rampUsers(10) over (10 seconds)),
      commentators.inject(rampUsers(2) over (10 seconds)),
      admins.inject(rampUsers(1) over (10 seconds))
    ).protocols(httpConf)

}