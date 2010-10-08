package org.scalatra

import org.scalatra._

import test.specs.ScalatraSpecification

class CsrfTokenServlet extends ScalatraServlet with CSRFTokenSupport {
  get("/renderForm") {
    """<html>
      <body>
        <form method="post"><input type="hidden" name="csrfToken" value="%s" /></form>
      </body>
    </html>""" format session("csrfToken").get
  }

  post("/renderForm") {
    "SUCCESS"
  }
}

object CSRFTokenSpec extends ScalatraSpecification {

  addServlet(classOf[CsrfTokenServlet], "/*")


  "the get request should include the CSRF token" in {
    get("/renderForm") {
      body must beMatching("""value="\w+""")
    }
  }

  "the post should be valid when it uses the right csrf token" in {
    var token = ""
    session {
      get("/renderForm") {
        token = ("value=\"(\\w+)\"".r findFirstMatchIn body).get.subgroups.head
      }
      post("/renderForm", "csrfToken" -> token) {
        body must be_==("SUCCESS")
      }
    }
  }

  "the post should be invalid when it uses a different csrf token" in {
    session {
      get("/renderForm") {
      }
      post("/renderForm", "csrfToken" -> "Hey I'm different") {
        status must be_==(403)
        body mustNot be_==("SUCCESS")
      }
    }
  }

}

// vim: set si ts=2 sw=2 sts=2 et: