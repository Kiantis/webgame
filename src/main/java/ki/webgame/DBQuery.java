package ki.webgame;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import static ki.webgame.DBManager.query;

public class DBQuery
{
    private Connection connection;
    private final String query;
    private final List<Object> parameters;

    public DBQuery(String query)
    {
        this.query = query;
        parameters = new ArrayList<>();
    }

    public DBQuery(Connection c, String query)
    {
        this.connection = c;
        this.query = query;
        parameters = new ArrayList<>();
    }
    
    public DBQuery addParameter(Object parameter)
    {
        parameters.add(parameter);
        return this;
    }
    
    public void execute() throws Exception
    {
        execute(null);
    }
    
    public void execute(DBManager.QueryDone queryDone) throws Exception
    {
        query(connection, query, parameters, queryDone);
    }
}
