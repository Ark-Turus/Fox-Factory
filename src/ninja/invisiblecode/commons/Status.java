package ninja.invisiblecode.commons;

public interface Status {

	public static final int	DEBUG	= 0x1000;
	public static final int	INFO	= 0x2000;
	public static final int	WARNING	= 0x3000;
	public static final int	ERROR	= 0x4000;

	public void update(int state, String message);

	public static class SystemStatus implements Status {

		@Override
		public void update(int state, String message) {
			switch (state) {
				case DEBUG:
					System.out.print("DEBUG:    ");
					break;
				case INFO:
					System.out.print("INFO:     ");
					break;
				case WARNING:
					System.out.print("WARNING:  ");
					break;
				case ERROR:
					System.err.print("ERROR:    ");
					break;
			}
			System.err.println(message);
			System.err.flush();
		}

	}

}
