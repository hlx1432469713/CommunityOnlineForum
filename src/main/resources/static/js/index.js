$(function(){
	$("#publishBtn").click(publish);
});

function publish() {

	// 发送AJAX请求之前,将CSRF令牌设置到请求的消息头中.
//    var token = $("meta[name='_csrf']").attr("content");
//    var header = $("meta[name='_csrf_header']").attr("content");
//    $(document).ajaxSend(function(e, xhr, options){
//        xhr.setRequestHeader(header, token);
//    });

	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	if(title == null || title == ''){
		alert("新帖标题为空！！");
		return;
	}
	if(content == null || content == ''){
		alert("新帖正文内容为空！！");
		return;
	}
	$("#publishModal").modal("hide");
	// 发送异步请求(POST)
	$.post(
		CONTEXT_PATH + "/discuss/addDiscussPost",
		{"title":title,"content":content},
		function(data) {
			data = $.parseJSON(data);
			// 在提示框中显示返回消息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2秒后,自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 刷新页面
				if(data.code == 200) {
					window.location.reload();
				}
			}, 2000);
		}
	);

}