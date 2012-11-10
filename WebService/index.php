<?

	$TAG_REPORT_QUESTION = "reportMistakeInQuestion";
	$TAG_UPDATE_FROM_DB = "updateFromDb";
	$TAG_UPDATE_WRONG_CORRECT = "updateWrongCorrectStat";
	$TAG_GET_LAST_UPDATE = "getLastUpdateQuestions";
	$TAG_GET_LAST_UPDATE_CATEGORIES = "getLastUpdateCategories";
	$TAG_GET_CATEGORIES = "getCategories";
	$TAG_USER_REGISTER = "tagUserRegister";
	$TAG_USER_LOGIN = "tagUserLogin";
	$TAG_USER_RECOVER_PASSWORD = "tagRecoverPassword";
	$TAG_SUGGEST_QUESTION = "tagSuggestQuestion";
	$TAG_UPLOAD_GAME_SCORE = "tagUploadGameScore";
	$TAG_DOWNLOAD_GAME_SCORE = "tagDownloadGameScores";
	$TAG_GET_TOP_TEN_HARDEST_Q = "tagGetTopTenHardest";

	$ERROR_USER_ALREADY_EXISTS = 1001;
	$ERROR_ADDING_USER = 1002;
	$ERROR_USER_DOES_NOT_EXISTS = 1003;
	$ERROR_USER_WRONG_PASSWORD = 1004;
	$ERROR_QUESTION_NOT_ADDED = 1005;
	$ERROR_SCORE_WAS_NOT_ADDED = 1006;

	$ERROR_MYSQL = 3001;

	$SUCCESS_USER_REGISTERED = 2001;
	$SUCCESS_USER_EXIST = 2002;
	$SUCCESS_QUESTION_ADDED = 2003;
	$SUCCESS_SCORE_WAS_ADDED = 2004;

	$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');
	mysql_query("SET NAMES 'utf8'");
	mysql_select_db('triviadb', $link);

	if ( isset($_POST['tag'])){
		$tag = $_POST['tag'];
	} else {
		$tag = "empty";
		$response = "";
	}

error_log($tag);

if ( !strcmp($tag,$TAG_UPDATE_FROM_DB)){

	$lastUserUpdate = 0;
	
	$lastUserUpdate = $_POST['lastUserUpdate'];

	$query = "SELECT * FROM questions WHERE colLastUpdate > $lastUserUpdate";
	$result = mysql_query($query,$link);
	$response = array();
	$num_of_rows = mysql_num_rows($result);

	if ( $num_of_rows > 0) {

		$response[] = array("success" => '1', "number_of_rows" => $num_of_rows);

		while ($post = mysql_fetch_assoc($result)) {
		
			$response[] = $post;
		}
	} else { 
		$response['error'] = 1;
	}

	if ( isset($_GET['format'])){
	
		header('Content-type: text/html; charset=utf-8');
	
	} else {

		header('Content-type: application/json; charset=utf-8');
	
	}
} else if ( !strcmp($tag,$TAG_REPORT_QUESTION)) {

	$response = array( );

	$questionId = $_POST['colQuestionId'];
	$colDescription = $_POST['colDescription'];

	$colDescription = mysql_real_escape_string($colDescription);

	//$question = utf8_encode($question);
	header('Content-Type: text/html; charset=utf-8');

	$query = "INSERT INTO mistakeInQuestion (colQuestionId,colDescription)
	VALUES
	('$questionId','$colDescription')";

	if ( !mysql_query($query, $link) ){
		die ('Error: ' . mysql_error());
		$response['error'] = 1;
		$response['errorDesc'] = mysql_error();
	} else {
		$response['success'] = 1;

	}


} else if ( !strcmp($tag,$TAG_UPDATE_WRONG_CORRECT)){

	$response = array( );

        $questionId = $_POST['colQuestionId'];
        $colWrongCounter = $_POST['colWrongCounter'];
        $colCorrectCounter = $_POST['colCorrectCounter'];

        //$question = utf8_encode($question);
        header('Content-Type: text/html; charset=utf-8');

        $query = "INSERT INTO questionWrongCorrect (colQuestionId,colWrongCounter,colCorrectCounter)
        VALUES
        ('$questionId','$colWrongCounter','$colCorrectCounter')";

        if ( !mysql_query($query, $link) ){
                die ('Error: ' . mysql_error());
                $response['error'] = 1;
        } else {
                $response['success'] = 1;
	}


} else if ( !strcmp($tag,$TAG_GET_LAST_UPDATE)){

	$userLastUpdate =  0;

	// checking if the user has given us the last update he did
	$userLastUpdate = $_POST['lastUserUpdate'];

	//error_log("last update is $userLastUpdate");


	$query = "SELECT COUNT(colLastUpdate)  FROM questions where  colLastUpdate > $userLastUpdate";

	//error_log($query);

	$result = mysql_query($query, $link);

	if ( !$result ){
                die ('Error: ' . mysql_error());
                $response['error'] = 1;
        } else {

		$response['success'] = 1;
		$row = mysql_fetch_array($result);
		$response['number'] = $row['COUNT(colLastUpdate)'];
		$query = "SELECT COUNT(colQuestionId)  FROM questions where  colQuestionId > $userLastUpdate";
		$result = mysql_query($query, $link);
		$row = mysql_fetch_array($result);
		$response['newQuestions'] = $row['COUNT(colQuestionId)'];


        }


} else if ( !strcmp($tag,$TAG_GET_LAST_UPDATE_CATEGORIES)){

	$userLastUpdate =  0;
        // checking if the user has given us the last update he did
	$userLastUpdate = $_POST['lastUserUpdate'];

        $query = "SELECT COUNT(colLastUpdate)  FROM categories where  colLastUpdate > $userLastUpdate";

        //error_log($query);

        $result = mysql_query($query, $link);

        if ( !$result ){
                die ('Error: ' . mysql_error());
                $response['error'] = 1;
        } else {

                $response['success'] = 1;
                $row = mysql_fetch_array($result);
                $response['number'] = $row['COUNT(colLastUpdate)'];

        }


} else if ( !strcmp($tag,$TAG_GET_CATEGORIES) ) {

        $lastUserUpdate = 0;

	$lastUserUpdate = $_POST['lastUserUpdate'];

        $query = "SELECT * FROM categories WHERE colLastUpdate > $lastUserUpdate";
        $result = mysql_query($query,$link);
        $response = array();
        $num_of_rows = mysql_num_rows($result);

        if ( $num_of_rows > 0) {

                $response[] = array("success" => '1', "number_of_rows" => $num_of_rows);

                while ($post = mysql_fetch_assoc($result)) {

                        $response[] = $post;
                }
        } else {
                $response['error'] = 1;
        }

} else if ( !strcmp($tag,$TAG_USER_REGISTER)){

	$username="";

	$username = $_POST['username'];
	$username = mysql_real_escape_string($username);
	$userType = $_POST['userType'];
	
	$response = array();
        $query = "SELECT * FROM users WHERE colUsername ='$username'";
        $result = mysql_query( $query, $link);

        $num_of_rows = mysql_num_rows($result);
	$response = array("success" => '0', "error" => 0);

	// checking if user already exists
	if ( $num_of_rows > 0 ) {
		// checking if it is a facebook user
		if ( !strcmp($userType,"1")) {
		
			$row = mysql_fetch_assoc($result);
			$response['success'] = $SUCCESS_USER_EXIST;
			$response['userId'] = $row['colUserId'];
			$response['userType'] = $row['colUserType'];
		} else { 
			$response = array("success" => '0', "error" => $ERROR_USER_ALREADY_EXISTS);
		}

	} else {
		$usermail = $_POST['usermail'];
		$usermail = mysql_real_escape_string($usermail);

		if ( !strcmp($userType,"0")) {
			$userpass = $_POST['userpass'];
			$userpass = mysql_real_escape_string($userpass);
			$userId = time();
		} else if ( !strcmp($userType,"1")) {
			$userpass = "facebook";
			$userId = $_POST['userId'];
		}

		
		$query = "INSERT INTO users (colUsername,colPassword,colEmail,colUserId,colUserType) VALUES ('$username','$userpass','$usermail','$userId','$userType')";
		$result = mysql_query($query,$link);
		
		if ( !$result ){
			die ('Error: ' . mysql_error());
			$response['error'] = $ERROR_ADDING_USER;
		} else { 
			$query = "SELECT colUserId from users WHERE colUsername = '$username'";
			$result = mysql_query($query, $link);
			if ( !$result ) {
				$response['error'] = $ERROR_MYSQL;
				$response['errorDesc'] = mysql_error();
			
			} else {
				$response['success'] = $SUCCESS_USER_REGISTERED;
				$row = mysql_fetch_assoc( $result );
				$response['userId'] = $row['colUserId'];
			}
		}

	}

} else if ( !strcmp($tag,$TAG_USER_LOGIN)){

	$username = $_POST['username'];
	$userpass = $_POST['userpass'];
	$username = mysql_real_escape_string($username);
	$userpass = mysql_real_escape_string($userpass);
	
        $query = "SELECT * FROM users WHERE colUsername ='$username'"; 
	//error_log($query);
        $result = mysql_query($query,$link);

        $num_of_rows = mysql_num_rows($result);
	$response = array("success" => '0', "error" => 0);

	// checking if user already exists
	if ( $num_of_rows > 0 ) {
		$row = mysql_fetch_assoc($result);
		if ( !strcmp($row['colPassword'],$userpass)){
			$response['success'] = $SUCCESS_USER_EXIST;
			$response['userId'] = $row['colUserId'];
			$response['userType'] = $row['colUserType'];
		} else {
			$response['error'] = $ERROR_USER_WRONG_PASSWORD;
		}
	} else {
		$response['error'] = $ERROR_USER_DOES_NOT_EXISTS;
	}

} else if ( !strcmp($tag,$TAG_USER_RECOVER_PASSWORD)) {

	$username = $_POST['username'];

        $response = array();
        $query = "SELECT * FROM users WHERE colUserName ='$username'";
        $result = mysql_query($query,$link);

        $num_of_rows = mysql_num_rows($result);
        $response = array("success" => '0', "error" => 0);

        // checking if user already exists
        if ( $num_of_rows > 0 ) {

		$row = mysql_fetch_assoc($result);

                $response['success'] = $SUCCESS_USER_EXIST;
		$response['userpass'] = $row['userPass'];
        } else {
                $response['error'] = $ERROR_USER_DOES_NOT_EXISTS;
        }

} else if ( !strcmp($tag, $TAG_SUGGEST_QUESTION)) {

	$userId = $_POST['userId'];
	$answerCorrect = $_POST['answerCorrect'];
	$answerQuestion = $_POST['answerQuestion'];
	$answerWrong1 = $_POST['answerWrong1'];
	$answerWrong2 = $_POST['answerWrong2'];
	$answerWrong3 = $_POST['answerWrong3'];

	$answerCorrect = mysql_real_escape_string($answerCorrect);
	$answerQuestion = mysql_real_escape_string($answerQuestion);
	$answerWrong1 = mysql_real_escape_string($answerWrong1);
	$answerWrong2 = mysql_real_escape_string($answerWrong2);	
	$answerWrong3 = mysql_real_escape_string($answerWrong3);

	$response = array("success" => '0', "error" => '0');

	// checking if user already exists
	$query = "INSERT INTO suggestedQuestions (userId,answerCorrect,answerQuestion,answerWrong1,answerWrong2,answerWrong3) values ('$userId','$answerCorrect','$answerQuestion','$answerWrong1','$answerWrong2','$answerWrong3')";
	$result = mysql_query($query,$link);
		
	if ( !$result ){
		die ('Error: ' . mysql_error());
		$response['error'] = $ERROR_QUESTION_NOT_ADDED;
	} else { 
		$response['success'] = $SUCCESS_QUESTION_ADDED;
	}

} else if ( !strcmp($tag, $TAG_UPLOAD_GAME_SCORE)) {

	$userId = $_POST['userId'];
        $gameType = $_POST['gameType'];
        $gameScore = $_POST['gameScore'];
        $gameTime = $_POST['gameTime'];

        $response = array("success" => '0', "error" => '0');

        // checking if user already exists
        $query = "INSERT INTO games (colUserId,colGameType,colGameScore,colGameTime) values ('$userId','$gameType','$gameScore','$gameTime')";
        $result = mysql_query($query,$link);

        if ( !$result ){
                die ('Error: ' . mysql_error());
                $response['error'] = $ERROR_SCORE_WAS_NOT_ADDED;
        } else {
                $response['success'] = $SUCCESS_SCORE_WAS_ADDED;
        }

} else if ( !strcmp($tag, $TAG_DOWNLOAD_GAME_SCORE)) {

	$gameType = $_POST['gameType'];

	$query = "SELECT r.colUsername,colGameType,colGameScore,colGameTime FROM games g INNER JOIN users r ON r.colUserId = g.colUserId ";

	if ( $gameType > 0 ) {
		// filter by game type
		//error_log ("game type is $gameType");
		$query = $query . " WHERE colGameType=$gameType";
	}

	$query = $query . " ORDER BY colGameScore DESC";

	
        $result = mysql_query($query,$link);

	if ( !$result ) {
		error_log ("error during query");
		$response = array();
		$response[] = array("error" => '1');
	} else {
		$response = array();
		$num_of_rows = mysql_num_rows($result);

		$response[] = array("success" => '1', "number_of_rows" => $num_of_rows);

		while ($post = mysql_fetch_assoc($result)) {
				$response[] = $post;
		}

	}

} else if ( !strcmp($tag, $TAG_GET_TOP_TEN_HARDEST_Q)) {

		$query = "
			SELECT colQuestionId  (
			colCorrect + colWrong
			) AS t
			FROM questions
			ORDER BY colCorrectWrongRatio DESC , t DESC 
			LIMIT 0 , 10";

		$result = mysql_query($query,$link);

		if ( !$result ) {
			error_log ("error during query $query");
			$respose = array();
			$response[] = array("error" => '1');
		} else {
			
			$response[] = array("success" => '1', "number_of_rows" => $num_of_rows);

			while ($post = mysql_fetch_assoc($result)) {
				$response[] = $post;
			}
		}

} else {

	error_log("TAG $tag wasn't found");

}

	echo json_encode($response);

	error_log(json_encode($response));

	mysql_close($link);

?>

