$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

//置顶按钮
function setTop() {
    $.post(
        CONTEXT_PATH + "/discussPost/top",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $("#topBtn").attr("disabled","disabled");
            }else{
                alert(data.message)
            }
        }
    );
}

function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discussPost/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else{
                alert(data.message)
            }
        }
    );
}

function setDelete() {
    $.post(
        CONTEXT_PATH + "/discussPost/delete",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                location.href = CONTEXT_PATH + "/index";
            }else{
                alert(data.message)
            }
        }
    );
}

function like(btn,entityType,entityId,entityUserId,postId) {
    //发送Post请求
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                //正常返回,改变这个按钮下面的子节点的内容
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.status == 1 ? '已赞' : '赞');

            } else {
                alert(data.msg);
            }
        }
    )
}
