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
	 发布微博：<textarea rows="10" cols="160" name="weibo"></textarea><input type="submit" value="发布"><br/>
  </form>
  <br/><br/>
  <form action="weiboServlet.do?type=query" method="post" id="queryForm" >
	  <span>
	  <input type="hidden" id="currPage" name="currPage" value="1"/>
	  <input type="hidden" id="size" name="size" value="10"/>
	       输入微博内容查找微博：<input size="120" name="queryString" id="queryString" value="${requestScope.queryString }"/><input type="button" value="搜索" onclick="javascript:gotoPage(1);"/>
	  </span>
  </form>
  
  <br/><br/><br/>
   当第前${results.currentPage }页，共：${results.rowCount }条数据,每页显示${results.pageSize }条数据。 
   <select name="size" id="selectSize">
     <option value="10"  <c:if test="${size == 10 }">selected="selected"</c:if>>10</option>
     <option value="50" <c:if test="${size == 50 }">selected="selected"</c:if>>50</option>
     <option value="100" <c:if test="${size == 100 }">selected="selected"</c:if>>100</option>
     <option value="200" <c:if test="${size == 200 }">selected="selected"</c:if>>200</option>
     <option value="500" <c:if test="${size == 500 }">selected="selected"</c:if>>500</option>
     <option value="1000" <c:if test="${size == 1000 }">selected="selected"</c:if>>1000</option>
  </select>
  <a href="javascript:gotoPage(1);">首页</a> <a href="javascript:gotoPage(${results.prePage });">上一页</a> <a href="javascript:gotoPage(${results.nextPage });">下一页</a> <a href="javascript:gotoPage(${results.pageCount});">尾页</a> 
  <table>
    <tr>
      <td>序号</td>
      <td>微博摘要</td>
      <td>发布时间</td>
      <td>操作</td>
    </tr>
    <tr>
      <td colspan="3">耗时： ${times}<br/><br/></td>
    </tr>
  <c:if test="${empty requestScope.results.row  }">没有相关结果。</c:if>
  
  <c:forEach items="${requestScope.results.row }" var="bean" varStatus="idx">
    <tr>
      <td>${idx.index + 1 }</td>
      <td width="60%">${bean.summary }</td>
      <td>${bean.publishDate }</td>
      <td><a href="javascript:void(0);" onclick="deleteWeibo('${bean.id}');">删除微博</a></td>
    </tr>
  </c:forEach>
  </table>
  <br/><br/>
  当第前${results.currentPage }页，共：${results.rowCount }条数据,每页显示${results.pageSize }条数据。<a href="javascript:gotoPage(1);">首页</a> <a href="javascript:gotoPage(${results.prePage });">上一页</a> <a href="javascript:gotoPage(${results.nextPage });">下一页</a> <a href="javascript:gotoPage(${results.pageCount});">尾页</a> 
</body>
<script type="text/javascript">
    function gotoPage(currPage) {
    	if (document.getElementById("queryString").value == "") {
    		return;
    	}
    	document.getElementById("currPage").value=currPage;
    	document.getElementById("size").value=document.getElementById("selectSize").value;
    	document.getElementById("queryForm").submit();
    }
    
    function deleteWeibo(id) {
    	document.getElementById("queryForm").action = "weiboServlet.do?type=delete&id="+id;
    	document.getElementById("queryForm").submit();
    }
</script>
</html>