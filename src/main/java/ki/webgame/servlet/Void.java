package ki.webgame.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * This one is just used to maintain the session active, does nothing.
 */
@WebServlet("/void")
public class Void extends HttpServlet
{
}
