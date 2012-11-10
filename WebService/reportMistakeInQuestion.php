<?
$response = array( "success" => 0, "error" => 0);

$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");

mysql_select_db('triviadb', $link);

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
} else {
	$response['success'] = 1;

}

mysql_close($link);

echo json_encode($response);

?>

