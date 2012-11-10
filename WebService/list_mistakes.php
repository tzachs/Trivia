<html>
<head>
<title>Mistake Questions</title>
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
	$query = "DELETE FROM mistakeInQuestion WHERE _id='$id'";
	$result = mysql_query($query,$link);
}


$query = "SELECT * FROM mistakeInQuestion";
$result = mysql_query($query,$link);


while ($row = mysql_fetch_assoc($result)){

	$qid = $row['colQuestionId'];
	$id = $row['_id'];
        $comment = $row['colDescription'];
	echo "<tr>\n";
	echo "<td><a href=\"http://23.23.238.181/updatequestion.php?id=$qid\">$qid</a></td>\n";
	echo "<td><a href=\"http://23.23.238.181/updatequestion.php?id=$qid\">$comment</a></td>\n";
	echo "<td><a href=\"http://23.23.238.181/list_mistakes.php?id=$id\">Delete mistake report</a></td>\n";
	echo "</tr>\n";


}


mysql_close($link);


?>
</table>

</body>
</html>
