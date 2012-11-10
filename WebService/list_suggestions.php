<html>
<head>
<title>Questions Suggestions</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
<table border='1'>
<?

$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");
mysql_select_db('triviadb', $link);

// checking if we need to delete some report before feteching the queries
if ( isset($_GET['id'])){
	$id = $_GET['id'];
	$query = "DELETE FROM suggestedQuestions WHERE _id='$id'";
	$result = mysql_query($query,$link);
}


$query = "SELECT * FROM suggestedQuestions";
$result = mysql_query($query,$link);

if ( mysql_num_rows($result) == 0 ) {
	echo "<tr><td>no rows found</td></tr>";
} else {

	while ($row = mysql_fetch_assoc($result)){

		$qid = $row['_id'];
		echo "<tr>\n";
		echo "<td><a href=\"http://23.23.238.181/new_question.php?id=$qid\">$qid</a></td>\n";
		echo "<td><a href=\"http://23.23.238.181/list_suggestions.php?id=$qid\">Delete suggestion</a></td>\n";
		echo "</tr>\n";

	}

}


mysql_close($link);


?>
</table>

</body>
</html>
