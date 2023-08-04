<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>그래프 통계 일괄 계산</title>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="https://code.jquery.com/jquery-1.12.4.js"></script>
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<link rel="shortcut icon" type="image/x-icon" href="/imgs/common/logo_img_ori.png">
<!-- <link rel="stylesheet" href="/css/login/login.css"> -->
<link rel="stylesheet" href="/css/common/layout.css">
<!-- <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css"> -->
<title>그래프 통계 일괄 계산</title>
<style type="text/css">
.ui-autocomplete {
	border: none;
	position: relative;
	background-color: #ffffff;
	list-style: none;
	font-size: 16px;
	margin-bottom: 5px;
	padding-top: 2px;
	border: 1px solid #DDD !important;
	padding-top: 0px !important;
	z-index: 1511;
	line-height: 45px;
	border-radius: 20px;
	text-align: center;
	width: 275px
}

.ui-menu-item {
	margin-left: -30px;
	border-bottom: 1px dotted;
	border-collapse: collapse;
}

.ui-helper-hidden-accessible {
	display: none;
}
</style>
</head>
<body>
		<input type="text" placeholder="기관명" id="schoolName" name="schoolName" >
		<input type="date" placeholder="측정일" id="measureDt" name="measureDt" >
		<input type="button" id="submit" value="그래프통계일괄계산">
		<input type="hidden" id="schoolCode" name="schoolCode" value="">
</body>
<script type="text/javascript">
	$('#submit').click(function() {
		var schoolCode = $('#schoolCode').val();
		var measureDt = $('#measureDt').val();

		$.ajax({
			type : 'POST', //post 방식으로 전송
			url : '/web/statistics/graphBatch.do', //데이터를 주고받을 파일 주소
			data : JSON.stringify({ //변수에 담긴 데이터를 전송해준다 (JSON 방식)
				"schoolCode" : schoolCode,
				"measureDt" : measureDt
			}),
			dataType : 'JSON', //데이터 타입 JSON
			contentType : "application/json; charset=UTF-8",
			success : function(data) { //파일 주고받기가 성공했을 경우. data 변수 안에 값을 담아온다.
				alert("그래프 통계 일괄 계산 성공");
			},
			error : function() { //실패
				alert("그래프 통계 일괄 계산 실패");
			}
		});
	});

	$('#schoolName').autocomplete({
		source : function(request, response) { //source: 입력시 보일 목록
			$.ajax({
				url : "/web/organ/ajaxSelectOrganList.do",
				type : "POST",
				dataType : "JSON",
				data : {
					value : request.term
				} // 검색 키워드
				,
				success : function(data) { // 성공
					response($.map(data.organList, function(item) {
						var seq = item.SEQ;
						var schoolCode = item.SCHOOL_CODE;
						var schoolName = item.SCHOOL_NAME;
						var organAddress = item.ORGAN_ADDRESS;
						return {
							label : schoolName + '\n\n(' + organAddress + ')' // 목록에 표시되는 값
							,
							value : schoolName // 선택 시 input창에 표시되는 값
							,
							idx : seq // index
							,
							schoolCode : schoolCode
						// 넘겨줄 값을 여기에 넣으면 됨
						};
					})); //response
				},
				error : function() { //실패
					alert("관리자에게 문의해주세요.");
				}

			});
		},
		minLength : 1,
		focus : function(event, ui) {
			return false;
		},
		select : function(event, ui) {
			$('#schoolCode').attr("value", ui.item.schoolCode);
		},
		delay : 300
	});
</script>
</html>