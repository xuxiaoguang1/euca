package com.eucalyptus.webui.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KeypairServlet extends HttpServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String hash = request.getParameter("hash");
    String[] pair = KeypairService.getInstance().select(hash);
    if (pair == null)
      return;
    String filename = pair[0] + ".key";
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"" );
    response.getWriter().write(pair[1]);
  }
  
}
