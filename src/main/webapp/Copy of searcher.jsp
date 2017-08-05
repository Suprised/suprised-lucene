<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
  b {
    color:red;
  }
</style>
</head>
<body>

	<form action="weiboServlet.do?type=send" method="post">
	  标题：<input size="120" name="title"/><br/>
	 内容：<textarea rows="10" cols="160" name="weibo"></textarea><br/>
	  <input type="submit" value="发布">
  </form>
  
  <form action="weiboServlet.do?type=query" method="post" id="queryForm" >
	  <span>
	  <input type="hidden" id="currPage" name="currPage" value="1"/>
	       输入微博内容查找微博：<input size="120" name="queryString" id="queryString" value="${requestScope.queryString }"/><input type="button" value="搜索" onclick="javascript:gotoPage(1);"/>
	  </span>
  </form>
  
  <br/><br/><br/>
  
  <c:if test="${empty requestScope.results.row  }">没有相关结果。</c:if>
  
  <c:forEach items="${requestScope.results.row }" var="bean" varStatus="idx">
    ${idx.index + 1 },<a href="${bean.url }">${bean.title }<br/>${bean.content }</a> <br/><br/>
  </c:forEach>
  
  当第前${results.currentPage }页，共：${results.rowCount }条数据,每页显示${results.pageSize }条数据。<a href="javascript:gotoPage(1);">首页</a> <a href="javascript:gotoPage(${results.prePage });">上一页</a> <a href="javascript:gotoPage(${results.nextPage });">下一页</a> <a href="javascript:gotoPage(${results.pageCount});">尾页</a> 
</body>
<script type="text/javascript">
    function gotoPage(currPage) {
    	if (document.getElementById("queryString").value == "") {
    		return;
    	}
    	document.getElementById("currPage").value=currPage;
    	document.getElementById("queryForm").submit();
    }
</script>
</html>