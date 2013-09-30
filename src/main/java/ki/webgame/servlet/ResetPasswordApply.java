package ki.webgame.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ki.webgame.DBQuery;
import static ki.webgame.DigestUtils.sha512;
import static ki.webgame.servlet.Login.SES_ATT_USERNAME;

@WebServlet("/resetpasswordapply")
public class ResetPasswordApply extends HttpServlet
{
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String username = request.getParameter("username");
        String code = request.getParameter("code");
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");
        
        if (username == null || password == null || password2 == null || code == null || 
            username.isEmpty() || password.isEmpty() || password2.isEmpty() || code.isEmpty() ||
            !password.equals(password2))
        {
            response.setStatus(500);
            return;
        }

        try
        {
            new DBQuery("select checkcode from users where username = ?")
                .addParameter(username)
                .execute((ResultSet rs) ->
                {
                    if (!rs.next())
                    {
                        response.setStatus(500);
                        response.sendRedirect("passwordreset_badcode.html");
                        return;
                    }
                    
                    new DBQuery("update users set password = ?, registered = 1, checkcode = null where username = ?")
                        .addParameter(sha512(password))
                        .addParameter(username)
                        .execute();
                    
                    // Automatically login
                    request.getSession().setAttribute(SES_ATT_USERNAME, username);
                });
        }
        catch (ServletException | IOException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ServletException(ex.getMessage(), ex);
        }
        
        response.setContentType("text/json");
        response.getWriter().write("true");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}
