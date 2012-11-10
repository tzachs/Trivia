<?
$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");
mysql_select_db('triviadb', $link);
$id = $_GET['id'];
$query = "SELECT * FROM questions WHERE colQuestionId='$id'";
$result = mysql_query($query,$link);

$row = mysql_fetch_assoc($result);

$question = $row['colQuestion'];
$question = htmlentities($question, ENT_QUOTES, 'UTF-8');

$colAnswer1 = $row['colAnswer1'];
$colAnswer1 = htmlentities($colAnswer1, ENT_QUOTES, 'UTF-8');
$colAnswer2 = $row['colAnswer2'];
$colAnswer2 = htmlentities($colAnswer2, ENT_QUOTES, 'UTF-8');
$colAnswer3 = $row['colAnswer3'];
$colAnswer3 = htmlentities($colAnswer3, ENT_QUOTES, 'UTF-8');
$colAnswer4 = $row['colAnswer4'];
$colAnswer4 = htmlentities($colAnswer4, ENT_QUOTES, 'UTF-8');
//echo $question;

mysql_close($link);

?>



<html>
<head>
<title>Update Questions</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body dir='rtl'>
<form action="insert.php" method="post">

<input type="hidden" name="tag" value="update"/>
<input type="hidden" name="questionId" value="<? echo $id ?>"/>
<table>
<tr>
<td>Question</td>
<td><input type="text" size="100" name="colQuestion" value="<? echo $question ?>"/></td>
</tr>
<tr>
<td>Answer</td>
<td><input type="text" size="100" name="colAnswer1" value="<? echo $colAnswer1 ?>"/></td>
</tr>
<tr>
<td>Wrong Answer 1</td>
<td><input type="text" size="100" name="colAnswer2" value="<? echo $colAnswer2 ?>" /></td>
</tr>
<tr>
<td>Wrong Answer 2</td>
<td><input type="text" size="100" name="colAnswer3" value="<? echo $colAnswer3 ?>" /></td>
</tr>
<tr>
<td>Wrong Answer 3</td>
<td><input type="text" size="100" name="colAnswer4"  value="<? echo $colAnswer4 ?>" /></td>
</tr>
<tr>
<td>Category</td>
<td>
<?

$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");
mysql_select_db('triviadb', $link);
$query = "SELECT * FROM categories";
$result = mysql_query($query,$link);

echo "<select name=\"colCategory\">\n";

$category = $row['colCategory'];

while ($row = mysql_fetch_assoc($result)){

	$name = $row['colHeName'];
	$id = $row['_id'];

	if ( strcmp($category,$id)) {

		echo "<option value=\"$id\">$name</option>\n";
	} else {
		echo "<option selected value=\"$id\">$name</option>\n";

	}

}


echo "</select>\n";

mysql_close($link);

?>
</td>
</tr>
<tr>
<td>Sub Category</td>
<td><input type="text" size="100" name="colSubCategory" />
</tr>
<tr><td><input type="submit" /></td></tr>
</table>
</form>

</body>
</html>
