<!DOCTYPE html>
<html>
<head>
<meta
	content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0"
	name="viewport" />
<meta content="yes" name="apple-mobile-web-app-capable" />
<meta content="black" name="apple-mobile-web-app-status-bar-style" />
<title><g:message code="title" /></title>

<asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
<asset:stylesheet src="stylesheets/lunch.css" />
<asset:javascript src="javascripts/lunch.js" />
</head>
<body>
	<div class="main">
		<div id="error" class="wrapper">
			<div class="message"></div>
			<div class="options">
				<div id="0" class="option">
					<g:message code="error.option.0" />
				</div>
			</div>
		</div>


		<div id="question" class="wrapper">
			<div class="message"></div>
			<div class="options"></div>
		</div>


		<div id="room" class="wrapper">
			<div class="message"></div>
			<div class="options">
				<div id="0" class="option">
					<g:message code="room.option.0" />
				</div>
				<div id="1" class="option">
					<g:message code="room.option.1" />
					<input type="hidden" id="roomList" />
				</div>
			</div>
		</div>
	</div>
</body>
</html>