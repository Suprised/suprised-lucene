package servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cloverworxs.common.Pagination;
import com.dascom.lucene.demo.LuceneIndexBean;
import com.dascom.lucene.demo.LuceneInstance;

/**
 * Servlet implementation class WeiboServlet
 */
@WebServlet("/WeiboServlet")
public class WeiboServlet2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private int index = 33;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WeiboServlet2() {
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
	    }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    doGet(request, response);
	}

	private void doSend(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException  {
	    LuceneInstance helper = LuceneInstance.Instance;
	    String content = request.getParameter("weibo");
	    LuceneIndexBean indexBean = new LuceneIndexBean();
	    indexBean.setUrl("http://www.baidu.com?key=123456789" + index++);
        indexBean.setContent(index++ + content);
        indexBean.setDate(new Date());
        indexBean.setKey("123456789" + index++);
        indexBean.setTitle(request.getParameter("title"));
        
        helper.createIndex(indexBean, true);
	    response.sendRedirect("searcher.jsp");
	}
	
	private void doQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException  {
	    String queryString = request.getParameter("queryString");
	    request.setAttribute("queryString", queryString);
	    LuceneInstance helper = LuceneInstance.Instance;
//	    List<LuceneIndexBean> beans = helper.query(queryString, 1, 10);
	    int currPage = Integer.parseInt(request.getParameter("currPage"));
	    Pagination page = helper.queryPage(queryString, currPage, 10);
	    request.setAttribute("results", page);
	    
	    request.getRequestDispatcher("searcher.jsp").forward(request, response);
	}
}
