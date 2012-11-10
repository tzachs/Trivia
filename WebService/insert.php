<?
$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");

mysql_select_db('triviadb', $link);

$question = $_POST['colQuestion'];
$questionId = time();
$answer1 = $_POST['colAnswer1'];
$answer2 = $_POST['colAnswer2'];
$answer3 = $_POST['colAnswer3'];
$answer4 = $_POST['colAnswer4'];
$category = $_POST['colCategory'];
if ( isset($_POST['grpLang'])) {
	$language = $_POST['grpLang'];
} else {
// TODO: change this default of hebrew
$language=2;
}

$answer1 = mysql_real_escape_string($answer1);
$answer2 = mysql_real_escape_string($answer2);
$answer3 = mysql_real_escape_string($answer3);
$answer4 = mysql_real_escape_string($answer4);
$question = mysql_real_escape_string($question);

$tag = $_POST['tag'];


header('Content-Type: text/html; charset=utf-8');

// checking if we need to update or insert the new question

if ( !strcmp($_POST['tag'],"update")){

	$questionId=$_POST['questionId'];
	$lastUpdate=time();

	$query = "UPDATE questions SET colQuestion='$question', colAnswer1='$answer1', colAnswer2='$answer2', colAnswer3='$answer3', colAnswer4='$answer4', colCategory='$category',  colLastUpdate='$lastUpdate', colEnabled='1' WHERE colQuestionId='$questionId'";


} else {

	$query = "INSERT INTO questions (colQuestionId,colQuestion,colAnswer1,colAnswer2,colAnswer3,colAnswer4,colAnswerIndex,colCategory,colLanguage,colLastUpdate,colEnabled)
	VALUES
	('$questionId','$question','$answer1','$answer2','$answer3','$answer4','1','$category','$language','$questionId','1')";
	}

	if ( !mysql_query($query, $link) ){
		error_log("Error inserting line: " . $query);
		die ('Error: ' . mysql_error());
	} else {
	$idSuggested =  $_POST['isSuggested'];
	if ( $idSuggested > -1 ){
		$query = "DELETE FROM suggestedQuestions WHERE _id='$idSuggested'";
		mysql_query($query);
	}
}
		

echo "<html>\n";
echo "<head><title>insert question</title></head>\n";
echo "<body dir=\"rtl\">\n";
echo "$question was $tag";
echo "<br />\n";
echo "<a href=\"http://23.23.238.181/new_question.php\">new question</a>\n";
echo "<br />\n";
echo "<a href=\"http://23.23.238.181/new_question.php?catid=$category\">new question - same category</a>\n";

echo "</body></html>";


mysql_close($link);

?>

