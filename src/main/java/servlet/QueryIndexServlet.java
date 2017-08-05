package servlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.cloverworxs.common.Pagination;
import com.dascom.lucene.example.manager.FileIndexManager;
import com.dascom.lucene.example.manager.FileIndexManagerImpl;

/**
 */
public class QueryIndexServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private FileIndexManager fileIndexManager = new FileIndexManagerImpl();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public QueryIndexServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String type = request.getParameter("type");
        if ("createIndex".equals(type)) {
            doCreateIndex(request, response);
        } else {
            doQuery(request, response);
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    

    private void doCreateIndex(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String dir = request.getParameter("indexDir");
        File file = new File(dir);
        if (file.exists()) {
            try {
                long beginTime = System.currentTimeMillis();
                fileIndexManager.addIndex(file);
                request.setAttribute("time", (new Date().getTime() - beginTime));
                request.setAttribute("success", true);
                request.setAttribute("totalFiles", FileIndexManagerImpl.atomicInt);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("success", false);
                request.setAttribute("errorMsg", e.getMessage());
            }
        } else {
            request.setAttribute("success", false);
            request.setAttribute("errorMsg", "目录不存在");
        }
        request.getRequestDispatcher("createIndex.jsp").forward(request, response);
    }
    
    private void doQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        QueryFilter filter = new QueryFilter();
        filter.setDesc(request.getParameter("desc"));
        filter.setKeyword(request.getParameter("keyword"));
        filter.setTitle(request.getParameter("title"));
        filter.setFileExts(request.getParameter("ext"));
        filter.setCondition(request.getParameter("condition"));
        
        int currPage = Integer.parseInt(request.getParameter("currPage"));
        int size = Integer.parseInt(request.getParameter("size"));
        long begin = new Date().getTime();
        Pagination page = fileIndexManager.findByIndex(filter.getExpress(), currPage, size);
        
        request.setAttribute("results", page);
        request.setAttribute("filter", filter);
        request.setAttribute("size", size);
        request.setAttribute("times", (new Date().getTime() - begin) + "ms");
        String gotoJsp = "searcher2.jsp";
        if (StringUtils.isNotBlank(filter.getCondition())){
            gotoJsp = "searcher3.jsp";
        }
        request.getRequestDispatcher(gotoJsp).forward(request, response);
    }

    public class QueryFilter {
        private String desc;
        private String keyword;
        private String title;
        private String fileExts;
        private String condition;
        
        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getFileExts() {
            return fileExts;
        }

        public void setFileExts(String fileExts) {
            this.fileExts = fileExts;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getExpress() {
            StringBuilder sbExpress = new StringBuilder();
            if (StringUtils.isNotBlank(this.title)) {
                sbExpress.append(this.title);
                sbExpress.append(" ");
            }
            if (StringUtils.isNotBlank(this.desc)) {
                sbExpress.append(this.desc);
                sbExpress.append(" ");
            }
            if (StringUtils.isNotBlank(this.keyword)) {
                sbExpress.append("keyword:" + this.keyword);
                sbExpress.append(" ");
            }
            if (StringUtils.isNotBlank(this.fileExts)) {
                sbExpress.append("path:*" + this.fileExts);
                sbExpress.append(" ");
            }
            if (StringUtils.isNotBlank(this.condition)) {
                sbExpress.append(this.condition);
            }
            return sbExpress.toString();
        }
    }
}
