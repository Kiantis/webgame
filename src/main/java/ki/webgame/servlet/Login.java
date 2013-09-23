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

@WebServlet("/login")
public class Login extends HttpServlet
{
    public static final String SES_ATT_USERNAME = "username";
    private static final String REQ_PAR_USERNAME = "username";
    private static final String REQ_PAR_PASSWORD = "password";
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setContentType("text/json");
        try
        {
            new DBQuery("select username from users where username = ? and password = ?")
                .addParameter(request.getParameter(REQ_PAR_USERNAME))
                .addParameter(sha512(request.getParameter(REQ_PAR_PASSWORD)))
                .execute((ResultSet rs) ->
            {
                boolean logged = rs.next();
                if (logged)
                    request.getSession().setAttribute(SES_ATT_USERNAME, request.getParameter(REQ_PAR_USERNAME));
                response.getWriter().write(String.valueOf(logged));
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
