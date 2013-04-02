package net.svcret.ejb.ejb;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.svcret.ejb.api.IChartingServiceBean;

import org.rrd4j.data.CubicSplineInterpolator;
import org.rrd4j.data.Plottable;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

public class ChartingServiceBean implements IChartingServiceBean {

	public static void main(String[] args) throws IOException {

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setVerticalLabel("km/h");
		// graphDef.datasource("myspeed", "./test.rrd", "speed",
		// ConsolFun.AVERAGE);
		
		long now = System.currentTimeMillis();
		long[] timestamps = new long[] {now - 20000, now - 10000, now};
		double[] values = new double[] { 1, 3, 2 };
		Plottable plottable = new CubicSplineInterpolator(timestamps, values);
		graphDef.datasource("test", plottable);

		graphDef.setTimeSpan(now - 20000, now);

		graphDef.line("test", Color.BLACK);
		
//		graphDef.datasource("test2", fetchData);
		
//		graphDef.datasource("kmh", "myspeed,3600,*");
//		graphDef.datasource("fast", "kmh,100,GT,100,0,IF");
//		graphDef.datasource("over", "kmh,100,GT,kmh,100,-,0,IF");
//		graphDef.datasource("good", "kmh,100,GT,0,kmh,IF");
//		graphDef.area("good", new Color(0, 0xFF, 0), "Good speed");
//		graphDef.area("fast", new Color(0x55, 0, 0), "Too fast");
//		graphDef.stack("over", new Color(0xFF, 0, 0), "Over speed");
		graphDef.hrule(100, new Color(0, 0, 0xFF), "Maximum allowed");
		graphDef.setFilename("./speed4.gif");
		RrdGraph graph = new RrdGraph(graphDef);
		BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		graph.render(bi.getGraphics());

		File outputfile = new File("saved.png");
		ImageIO.write(bi, "png", outputfile);

	}

}
