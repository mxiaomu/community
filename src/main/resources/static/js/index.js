$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
    // 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    /*var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e,xhr,option) {
        xhr.setRequestHeader(header,token);
    });*/
	$("#publishModal").modal("hide");
	// 获取消息和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发布异步请求
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{
			"title":title,
			"content":content
		},
		function (data) {
			data = $.parseJSON(data);
			console.log(data);
			// 在提示框里显示返回消息
			$("#hintBody").text(data.msg);
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if (data.code===0){
					window.location.reload();
				}
			}, 2000);
		}
	)

}