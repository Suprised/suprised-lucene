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

	<form action="queryIndexServlet.do?type=createIndex" name="form" id="form1" method="post" >
	  本地要索引目录：<input name="indexDir" size="80"/> <input type="button" onclick="doSubmit();" id="submit1" value="创建索引"/> 例如：F:\工作文件\lucene 
  </form>
  
  <c:if test="${success }">
           索引创建成功：总共索引文件：${totalFiles }个, 耗时：${time }ms.<br/> 
  </c:if>
  <a href="<%=request.getContextPath() %>/searcher2.jsp">转到搜索页面</a>
  <div id="msg"></div>
  <c:if test="${!success }">
    <font color="red">${errorMsg }</font>
  </c:if>
<script type="text/javascript">
   function doSubmit() {
	   var button = document.getElementById("submit1");
	   button.disabled = "disabled";
	   document.getElementById("msg").innerText = "正在创建索引，请稍后....";
	   var form = document.getElementById("form1");
	   form.submit();
   }
</script>
</body>
</html>