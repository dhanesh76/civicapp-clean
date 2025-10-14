package com.visioners.civic.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = "text/html")
    public String root() {
        String imageUrl = "https://www.looper.com/img/gallery/the-ending-of-interstellar-explained/intro-1562880872.jpg";

        return "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'/>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'/>\n" +
                "  <title>Civic API Live</title>\n" +
                "  <style>\n" +
                "    * { margin:0; padding:0; box-sizing:border-box; }\n" +
                "    html, body { height:100%; width:100%; overflow:hidden; }\n" +
                "    body {\n" +
                "      background: url('" + imageUrl + "') center center / cover no-repeat;\n" +
                "      display:flex;\n" +
                "      align-items:center;\n" +
                "      justify-content:center;\n" +
                "      color:#fff;\n" +
                "      text-shadow: 0 0 10px rgba(0,0,0,0.7);\n" +
                "      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "      position: relative;\n" +
                "    }\n" +
                "    .overlay {\n" +
                "      position:absolute; top:0; left:0; width:100%; height:100%;\n" +
                "      background: rgba(0,0,0,0.5); backdrop-filter: blur(2px);\n" +
                "      z-index: 0;\n" +
                "    }\n" +
                "    .content {\n" +
                "      position: relative; z-index:1;\n" +
                "      text-align:center;\n" +
                "      max-width:80%; padding:20px;\n" +
                "    }\n" +
                "    h1 { font-size:3em; margin-bottom:0.5em; }\n" +
                "    p { font-size:1.2em; }\n" +
                "    @media (max-width:600px){ h1{font-size:2em;} p{font-size:1em;} }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class='overlay'></div>\n" +
                "  <div class='content'>\n" +
                "    <h1>Civic API: Live Among the Stars</h1>\n" +
                "    <p>⭐ If you see this, the backend is up — database synced · Redis caching · Auth alive ⭐</p>\n" +
                "    <p style='margin-top:50px; font-size:0.9em; color:#ccc;'>From Team Visioners</p>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/home")
    public String home() {
        return "Yes, you are in the right place";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "Welcome Admin!";
    }
}
