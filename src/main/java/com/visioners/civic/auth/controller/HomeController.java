package com.visioners.civic.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = "text/html")

        public String root() {
            String imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRjvdIHCX1SkxYLWvXm55ZlapDkkWODwnUosQ&s";

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
            "      background: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)),\n" +
            "                  url('" + imageUrl + "') center center / cover no-repeat;\n" +
            "      background-color: #000;\n" +
            "      display:flex;\n" +
            "      align-items:center;\n" +
            "      justify-content:center;\n" +
            "      color:#fff;\n" +
            "      text-shadow: 0 0 10px rgba(0,0,0,0.7);\n" +
            "      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
            "      position: relative;\n" +
            "    }\n" +
            "    @media (max-aspect-ratio: 1/1) { body { background-size: contain; background-position: center top; } }\n" +
            "    .overlay { position:absolute; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.5); backdrop-filter: blur(2px); z-index: 0; }\n" +
            "    .content { position: relative; z-index:1; text-align:center; max-width:80%; padding:20px; }\n" +
            "    h1 { font-size:3em; margin-bottom:0.5em; }\n" +
            "    p { font-size:1.2em; }\n" +
            "    @media (max-width:600px){ h1{font-size:2em;} p{font-size:1em;} }\n" +
            "    /* Civic Pulse animation */\n" +
            "    .pulse { display:inline-block; width:20px; height:20px; margin:0 5px; background:#ffcc00; border-radius:50%;\n" +
            "             animation: pulseAnim 1.5s infinite ease-in-out; }\n" +
            "    .pulse:nth-child(2) { animation-delay: 0.3s; }\n" +
            "    .pulse:nth-child(3) { animation-delay: 0.6s; }\n" +
            "    @keyframes pulseAnim {\n" +
            "      0%, 80%, 100% { transform: scale(0); opacity:0.3; }\n" +
            "      40% { transform: scale(1); opacity:1; }\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "  <div class='overlay'></div>\n" +
            "  <div class='content'>\n" +
            "    <h1>Civic API: Live Among the Stars</h1>\n" +
            "    <p>⭐ Backend is alive — DB synced · Redis caching · Auth operational ⭐</p>\n" +
            "    <p style='margin-top:20px;'>Civic Pulse:</p>\n" +
            "    <div style='margin-top:10px;'>\n" +
            "      <span class='pulse'></span>\n" +
            "      <span class='pulse'></span>\n" +
            "      <span class='pulse'></span>\n" +
            "    </div>\n" +
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
