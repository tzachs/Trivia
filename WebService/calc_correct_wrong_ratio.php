<html>
<head>
<title>Update Questions Ratio</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body dir='ltr'>


<?
$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");
mysql_select_db('triviadb', $link);

$query = "SELECT colQuestionId, colWrong, colCorrect FROM questions";
$result = mysql_query($query,$link);

$number_of_rows = mysql_num_rows($result);

while ( $row = mysql_fetch_array($result)){

	$id = $row['colQuestionId'];
        $wrong = $row['colWrong'];
        $correct = $row['colCorrect'];
	$time = time();


	$total = $wrong + $correct;

	// ratio between 0.1 - 1
	// where 0.1 is the easiest question and 1 is the hardest

	if ( $wrong == 0){
		$ratio = 0.1;
	} else if ( $correct == 0 ){
		// question has never been answered correctly
		$ratio = 1;
	} else {
		$ratio = $wrong / $total;
	}	

	$query = "UPDATE questions SET colCorrectWrongRatio=$ratio, colLastUpdate='$time' WHERE colQuestionId='$id'";
	if ( !mysql_query($query,$link)){

                echo "Error update $id";

        } else {
		echo "update question id $id with ratio $ratio ( $wrong / $total )<br />\n";

	}

}

echo "updated $number_of_rows <br />\n";
mysql_close($link);

?>


</body>
</html>
