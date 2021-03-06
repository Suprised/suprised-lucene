<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
</style>
</head>
<body>

	<form action="queryIndexServlet.do" id="queryForm" method="post">
	<input type="hidden" id="currPage" name="currPage" value="1"/>
	<input type="hidden" id="size" name="size" value="10"/>
	<table>
	 <tr>
	   <td><input size="100" id="condition" name="condition" value="${filter.condition }"/></td>
	   <td><input type="button" onclick="gotoPage(1);" value="搜索"/>(搜索文件路径和内容中有关键字的文件)</td>
	 </tr>
	</table>
  </form>
  
  <br/><br/>
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
  <br/><br/><br/>
  <table>
    <tr>
      <td>序号</td>
      <td>文件路径</td>
      <td>最后修改时间</td>
    </tr>
    <c:if test="${not empty requestScope.results.row  }">
    <tr><td colspan="3">耗时： ${times}<br/><br/></td></tr>
  <c:forEach items="${requestScope.results.row }" var="bean" varStatus="idx">
    <tr>
      <td>${idx.index + 1 }</td>
      <td><a href="file:${bean.path }">${bean.path }</a></td>
      <td>${bean.lastModifiedDate }</td>
    </tr>
  </c:forEach>
  </c:if>
  </table>
  <c:if test="${empty requestScope.results.row  }">没有相关结果。</c:if> <br/><br/>
  当第前${results.currentPage }页，共：${results.rowCount }条数据,每页显示${results.pageSize }条数据。<a href="javascript:gotoPage(1);">首页</a> <a href="javascript:gotoPage(${results.prePage });">上一页</a> <a href="javascript:gotoPage(${results.nextPage });">下一页</a> <a href="javascript:gotoPage(${results.pageCount});">尾页</a>
</body>
<script type="text/javascript">
    function gotoPage(currPage) {
    	var flag = false;
    	if (document.getElementById("condition").value != "") {
    		  flag = true;
    	}
    	if (!flag) return false;
    	document.getElementById("currPage").value=currPage;
    	document.getElementById("size").value=document.getElementById("selectSize").value;
    	document.getElementById("queryForm").submit();
    }
</script>
</html>