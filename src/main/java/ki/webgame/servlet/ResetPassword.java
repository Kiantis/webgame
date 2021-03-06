package ki.webgame.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ki.webgame.DBQuery;
import static ki.webgame.DigestUtils.sha512;
import ki.webgame.MailManager;

@WebServlet("/resetpassword")
public class ResetPassword extends HttpServlet
{
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String username = request.getParameter("username");
        
        if (username == null || username.isEmpty())
        {
            response.setStatus(500);
            return;
        }

        try
        {
            String newcode = sha512(
                String.valueOf(
                    System.currentTimeMillis() + 
                    new Random(System.currentTimeMillis()).nextDouble()
                )).substring(0, 5);

            new DBQuery("update users set checkcode = ? where username = ?")
                .addParameter(newcode)
                .addParameter(username)
                .execute();
            
            new DBQuery("select email from users where username = ?")
                .addParameter(username)
                .execute((ResultSet rs) -> 
                {
                    if (!rs.next())
                        return;
                    MailManager.sendResetPasswordEmail(username, rs.getString(1), newcode);
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
