package ESPReaderConsola;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;

public class Utildades {
	//Probar conexion ip para windows y linux
	public static boolean pingTest(String ip) {
		if (ip.indexOf(":") != -1)
			ip = ip.split(":")[0];

		if (System.getProperty("os.name").toLowerCase().indexOf("win") != -1) // W
		{
			try {
				Process p = Runtime.getRuntime().exec(
						"ping " + ip + " -n 1 -w 400");
				InputStreamReader ir = new InputStreamReader(p.getInputStream());
				LineNumberReader input = new LineNumberReader(ir);

				String str = input.readLine();
				while (str != null && str != "") {
					if (str.indexOf("Request timed out.") != -1) {
						return false;
					}
					str = input.readLine();
				}
				return true;

			} catch (IOException e) {
				return false;
			}

		} else // L
		{
			try {
				return InetAddress.getByName(ip).isReachable(300);
			} catch (Exception e) {
				return false;
			}
		}

	}
}
