//= require jquery
//= require_self

$(function() {
	init();
});

function ajax(url, data, callback) {
	$.ajax({
		url : url,
		data : data,
		async : false,
		dataType : "json",
		type : "POST",
		success : callback
	});
}

function init() {
	$("#error .option#0, #room .option#0").click(function() {
		ajax("lunch/first", "", function(data) {
			getQuestionOrRoom(data);
		});
	});

	$("#room .option#1").click(function() {
		ajax("lunch/change", {
			roomList : $("#roomList:hidden").val()
		}, function(data) {
			getQuestionOrRoom(data);
		});
	})

	ajax("lunch/first", "", function(data) {
		getQuestionOrRoom(data);
	});
}

function getQuestionOrRoom(data) {
	$(".wrapper").hide();
	$(".wrapper .message, #question .options").html("");

	if (data.status) {
		if (data.type == "question") {
			var question = data.question
			var options = question.options
			$("#question .message").html(question.text)

			for (x in options) {
				var option = options[x]
				var div = $("<div>").html(option.text).attr("id", option.id)
						.addClass("option");
				$("#question .options").append(div);
			}

			$("#question .option").click(function() {
				ajax("lunch/answer", {
					id : $(this).attr("id"),
					history : data.history
				}, function(data) {
					getQuestionOrRoom(data);
				});
			});
			$("#question").fadeIn();
		} else if (data.type == "room") {
			$("#room .message").html(data.room);
			$("#roomList:hidden").val(data.roomList)
			if (data.roomList.length == 0) {
				$("#room .option#1").hide();
			} else {
				$("#room .option#1").show();
			}
			$("#room").fadeIn();
		}
	} else {
		$("#error .message").html(data.message);
		$("#error").fadeIn();
	}

}
