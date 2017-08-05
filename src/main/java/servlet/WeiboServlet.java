package servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cloverworxs.common.Pagination;
import com.dascom.lucene.example.bean.WeiboIndexBean;
import com.dascom.lucene.example.manager.WeiboIndexManager;
import com.dascom.lucene.example.manager.WeiboIndexManagerImpl;

/**
 * Servlet implementation class WeiboServlet
 */
public class WeiboServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private WeiboIndexManager manager = new WeiboIndexManagerImpl();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WeiboServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    request.setCharacterEncoding("utf-8");
	    String type = request.getParameter("type");
	    if ("send".equals(type)) {
	        doSend(request, response);
	    } else if ("query".equals(type)) {
	        doQuery(request, response);
	    } else if ("delete".equals(type)){
	        manager.deleteIndex(request.getParameter("id"));
	        doQuery(request, response);
	    }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    doGet(request, response);
	}

	private void doSend(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException  {
	    WeiboIndexBean indexBean = new WeiboIndexBean();
	    indexBean.setId(Identities.uuid());
	    indexBean.setContent(request.getParameter("weibo"));
	    manager.addIndex(indexBean);
	    response.sendRedirect("searcher.jsp");
	}
	
	private void doQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException  {
	    String queryString = request.getParameter("queryString");
	    int currPage = Integer.parseInt(request.getParameter("currPage"));
        int size = Integer.parseInt(request.getParameter("size"));
        long begin = new Date().getTime();
	    Pagination page = manager.findByIndex(queryString, currPage, size);
	    
	    request.setAttribute("times", (new Date().getTime() - begin) + "ms");
	    request.setAttribute("queryString", queryString);
	    request.setAttribute("results", page);
	    request.setAttribute("size", size);
	    request.getRequestDispatcher("searcher.jsp").forward(request, response);
	}
}
