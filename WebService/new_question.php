<?

$colQuestion="";
$colAnswer="";
$colWrongAnswer1="";
$colWrongAnswer2="";
$colWrongAnswer3="";
$id=-1;

$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");
mysql_select_db('triviadb', $link);

if ( isset($_GET['id'])) {
	$id = $_GET['id'];
	$query="SELECT * FROM suggestedQuestions where _id='$id'";

	$result = mysql_query($query, $link);

	if ( !$result ) {
		die ("Error during select" . mysql_error());
	} else {
		$row = mysql_fetch_assoc($result);
		$colQuestion = $row['answerQuestion'];
		//error_log($colQuestion);
		$colAnswer = $row['answerCorrect'];
		$colWrongAnswer1 = $row['answerWrong1'];
		$colWrongAnswer2 = $row['answerWrong2'];
		$colWrongAnswer3 = $row['answerWrong3'];
	}
} 

?>

<html>
<head>
<title>New Questions</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="css/mystyle.css" />
</head>
<body id="bodyNewQ" class="dir_rtl">
<form action="insert.php" method="post">

	<input type="hidden" name="tag" value="insert"/>
	<input type="hidden" name="isSuggested" value="<?= $id ?>" />
	<table>
	<tr>
	<td>Question</td>
	<td><input type="text" size="100" name="colQuestion" value="<?= $colQuestion ?>"/></td>
	</tr>
	<tr>
	<td>Answer</td>
	<td><input type="text" size="100" name="colAnswer1" value="<?= $colAnswer ?>"/></td>
	</tr>
	<tr>
	<td>Wrong Answer 1</td>
	<td><input type="text" size="100" name="colAnswer2" value="<?= $colWrongAnswer1 ?>" /></td>
	</tr>
	<tr>
	<td>Wrong Answer 2</td>
	<td><input type="text" size="100" name="colAnswer3" value="<?= $colWrongAnswer2 ?>" /></td>
	</tr>
	<tr>
	<td>Wrong Answer 3</td>
	<td><input type="text" size="100" name="colAnswer4" value="<?= $colWrongAnswer3 ?>"/></td>
	</tr>
	<tr>
	<td>Category</td>
	<td>
<?

function printSubCategories( $id, $name, $link, $chosenId ){

	$querySub = "SELECT * FROM categories WHERE colParentId=$id ORDER BY colHeName ";
	//error_log($querySub);
	$resultSub = mysql_query ($querySub,$link);

	if ( $resultSub ) {
		while ($rowSub = mysql_fetch_assoc($resultSub)){

			$nameSub = $rowSub['colHeName'];
			$idSub = $rowSub['_id'];


			echo "<option value=\"$idSub\" ";
			if ( $idSub == $chosenId ){
				echo "selected";
			}
			echo ">&nbsp;&nbsp;  $name - $nameSub</option>\n";

		}
	} else {
		error_log("error fetching sub " . $resultSub );
	}

}


 // *** main ***

$chosenId = -2;

if ( isset($_GET['catid'])) {
	$chosenId = $_GET['catid'];
}

// getting the root categories
$query = "SELECT * FROM categories WHERE colParentId='-1' ORDER BY colHeName ";
$result = mysql_query($query,$link);

echo "<select name=\"colCategory\">\n";

while ($row = mysql_fetch_assoc($result)){

	$name = $row['colHeName'];
	$id = $row['_id'];

	echo "<option value=\"$id\" ";
	if ( $id == $chosenId ) {
		echo "selected";
	}
	echo ">$name</option>\n";
	printSubCategories ( $id , $name , $link , $chosenId );

}

mysql_close($link);

echo "</select>\n";

?>
</td>
</tr>
<tr>
  <td>Language</td>
  <td>
     <input type="radio" name="grpLang" value="2" checked>Hebrew <br />
     <input type="radio" name="grpLang" value="1">English <br />
</td>
</tr>
<tr><td><input type="submit" /></td></tr>
</table>
</form>

<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/changeLang.js"></script>
</body>
</html>
