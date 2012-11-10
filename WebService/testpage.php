<?
$link = mysql_connect('localhost','root','bella@123!') or die('Cannot connect to the DB');

mysql_select_db('triviadb', $link);

$query = "SELECT * FROM questions";

mysql_query("SET NAMES 'utf8'");

$result = mysql_query($query,$link);

$posts = array();

$num_of_rows = mysql_num_rows($result);

if ( $num_of_rows > 0) {
	header('Content-type: text/html; charset=utf-8');

    echo "<html>";
    echo "<head><title>test page</title></head>";
    echo "<body>";


	while ($post = mysql_fetch_assoc($result)) {

	    echo $post["colQuestion"];
	    echo "<br />";
		
		$posts[] = $post;
	}

    echo "</body>";
    echo "</html>";
}

mysql_close($link);

?>

