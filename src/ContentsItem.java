public class ContentsItem
{
	public final String text;
	public final int    lineNumber;

	public ContentsItem(String text, int lineNumber)
	{
		this.text = text;
		this.lineNumber = lineNumber;
	}

	@Override
	public String toString()
	{
		return lineNumber + " " +  text;
	}
}
