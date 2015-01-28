/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package mpicbg.trakem2.transform;

import java.lang.reflect.Method;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 * @author Stephan Saalfeld <saalfelds@janelia.hhmi.org>
 */
public class ThinPlateSplineTransformTest extends ThinPlateSplineTransform {

	final static private int n = 2000;
	private static double[] doubles;
	private static Method encode;
	private static Method decode;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final Random rnd = new Random(2000);
		doubles = new double[n];
		for (int i = 0; i < n; ++i) {
			do {
				doubles[i] = Double.longBitsToDouble(rnd.nextLong());
			} while (Double.isNaN(doubles[i]));
		}
		encode = ThinPlateSplineTransform.class.getDeclaredMethod("encodeBase64", double[].class);
		encode.setAccessible(true);
		decode = ThinPlateSplineTransform.class.getDeclaredMethod("decodeBase64", String.class, Integer.TYPE);
		decode.setAccessible(true);
	}

	@Test
	public void testEncodeBase64() {
		try {
			final String base64 = (String)encode.invoke(null, doubles);
			final double[] compare = (double[])decode.invoke(null, base64, n);
			Assert.assertArrayEquals(doubles, compare, 0.00001);
			Assert.assertEquals(doubles.length, compare.length);
		} catch (final Exception e) {
			e.printStackTrace(System.err);
			Assert.fail(e.getLocalizedMessage());
		}
	}

}
