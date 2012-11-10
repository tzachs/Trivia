<html>
<head>
<title>Update Questions Categories</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>

<body dir='rtl'>
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


function printQuestions () {


	
	$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');
	mysql_query("SET NAMES 'utf8'");
	mysql_select_db('triviadb', $link);
	$query = "SELECT * FROM questions";

	$result = mysql_query($query, $link);
	

	echo "<table>\n";
	while ($row = mysql_fetch_assoc($result)){

		$qid = $row['_id'];
		$question = $row['colQuestion'];
		$category = $row['colCategory'];
		
		echo "<form action=\"update_categories.php\" method=\"post\">\n";
		echo "<tr>\n";
		echo "<td>$qid </td>\n";
		echo "<td>$question</td>\n";
		echo "<td>\n";
		printCategory( $category );
		echo "</td>\n";
		echo "<input type=\"hidden\" name=\"qid\" value=\"$qid\">\n";
		echo "<td><input type=\"submit\" text=\"change\"></td>\n";
		echo "</form>\n";

	}
	echo "</table>\n";
}

function printCategory  ( $catid ) {

 // *** main ***

$chosenId = $catid;

if ( isset($_GET['catid'])) {
	$chosenId = $_GET['catid'];
}

$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_query("SET NAMES 'utf8'");
mysql_select_db('triviadb', $link);
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
}

function checkIfToUpdate() {

if ( isset($_POST['qid']) ) {

	$qid = $_POST['qid'];
	$colid = $_POST['colCategory'];
	$time = time();

	$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');
	mysql_query("SET NAMES 'utf8'");
	mysql_select_db('triviadb', $link);
	$query = "UPDATE questions SET colCategory='$colid',colLastUpdate='$time' WHERE _id='$qid'";
	
	$result = mysql_query( $query, $link );

	if ( !$result ) {
		die ("error update category");
	} else {
		echo  "updated question $qid with $colid\n";
	}	

	mysql_close ( $link );
}






}

checkIfToUpdate();
printQuestions();
?>

</body>
</html>
