<html>
<head>
<title>Update Questions</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body dir='ltr'>
<?
$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");
mysql_select_db('triviadb', $link);

$query = "SELECT colQuestionId, SUM(colWrongCounter) , SUM(colCorrectCounter) 
FROM questionWrongCorrect
GROUP BY colQuestionId";
$result = mysql_query($query,$link);

$number_of_rows = mysql_num_rows($result);

//echo "number of rows $number_of_rows <br />";

while ( $row = mysql_fetch_array($result)){

	$id = $row['colQuestionId'];
        $wrong = $row['SUM(colWrongCounter)'];
        $correct = $row['SUM(colCorrectCounter)'];

	//echo "colQuestionId = $id <br />";

	$time = time();

	$query = "UPDATE questions SET colCorrect=colCorrect+$correct, colWrong=colWrong+$wrong, colLastUpdate='$time' WHERE colQuestionId='$id'";
	if ( !mysql_query($query,$link)){

                echo "Error update $id";

        } else {
		// calc new ration
		$query = "SELECT colQuestionId, colWrong, colCorrect FROM questions WHERE colQuestionId='$id'";
		$result1 = mysql_query($query,$link);

		if ( !$result1 ) {
			die("Could not update ratio for $id");
		} else {

			$row1 = mysql_fetch_array($result1);
			$id = $row1['colQuestionId'];
			$wrong = $row1['colWrong'];
			$correct = $row1['colCorrect'];
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

				$query = "DELETE FROM questionWrongCorrect WHERE colQuestionId='$id'";
				if ( !mysql_query($query,$link)){

					echo "Error deleting $id from questionWrongCorret";
				}
			}
		}
	}
}

mysql_close($link);


?>


<?
echo "updated $number_of_rows <br />\n";
?>



</body>
</html>
