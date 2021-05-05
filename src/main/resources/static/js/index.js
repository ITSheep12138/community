$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//发送异步请求
	//1. 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//利用Jquery发送异步请求
	$.post(
		CONTEXT_PATH+"/discussPost/add",
		{"title":title,"content":content},
		function (data) {
			//将数据转成JSON对象
			data = $.parseJSON(data);
			//在提示框中显示返回的消息
			$("#hintBody").text(data.msg);

			//显示两秒中的提示信息
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if (data.code == 0){
					//如果成功发布数据，则刷新当前页面
					window.location.reload();
				}
			}, 2000);
		}
	);
}
