<!doctype html>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link type="text/css" rel="stylesheet" href="../AdminPortal.css">

    <script type="text/javascript" src="../js/jquery-1.10.0.min.js"></script>
    <script type="text/javascript" src="../js/jquery.sparkline.min-2.1.1.js"></script>
    <script type="text/javascript" src="../js/overlib.js"></script>
    <script type="text/javascript" src="../js/tinymce.min.js"></script>
    <script type="text/javascript" src="../js/vkbeautify.0.99.00.beta.js"></script>
    <script type="text/javascript" src="../AdminPortal/AdminPortal.nocache.js"></script>

	<!-- SyntaxHighliter -->
    <script type="text/javascript" src="../js/shCore.js"></script>
	<script type="text/javascript" src="../js/shBrushJScript.js"></script>
	<script type="text/javascript" src="../js/shBrushXml.js"></script>
	<script type="text/javascript" src="../js/shAutoloader.js"></script>
	
	<link href="../css/shCore.css" rel="stylesheet" type="text/css" />
	<link href="../css/shThemeDefault.css" rel="stylesheet" type="text/css" />
 
    <script type="text/javascript">
    
    	SyntaxHighlighter.autoloader('js ../js/shBrushJScript.js');
		SyntaxHighlighter.autoloader('xml ../js/shBrushXml.js');
		// SyntaxHighlighter.all();
    
		function jsDrawUsageSparkline(theElement, theValues, theHeight, theWidth) {
			var sparkOptions = new Array();
			sparkOptions['chartRangeMin'] = 0;
			sparkOptions['height'] = theHeight;
			sparkOptions['width'] = theWidth;
			sparkOptions['type'] = 'bar';
			sparkOptions['stackedBarColor'] = ['#0C0', '#66C', '#F00', '#FA0'];
			sparkOptions['barWidth'] = 2;
			sparkOptions['barSpacing'] = 0;
			sparkOptions['zeroColor'] = '#CCF';
			sparkOptions['disableTooltips'] = true;
			sparkOptions['disableInteraction'] = true;
			$(theElement).sparkline(theValues, sparkOptions);
		}
		
		function jsDrawMemorySparkline(theElement, theValues, theHeight, theWidth) {
			var sparkOptions = new Array();
			sparkOptions['chartRangeMin'] = 0;
			sparkOptions['height'] = theHeight;
			sparkOptions['width'] = theWidth;
			sparkOptions['type'] = 'bar';
			sparkOptions['stackedBarColor'] = ['#BB8', '#FFA'];
			sparkOptions['barWidth'] = 2;
			sparkOptions['barSpacing'] = 0;
			sparkOptions['zeroColor'] = '#CCF';
			sparkOptions['disableTooltips'] = true;
			sparkOptions['disableInteraction'] = true;
			$(theElement).sparkline(theValues, sparkOptions);
		}

		function jsDrawRecentMonitorTestsSparkline(theElement, theValues, theHeight, theWidth) {
			var sparkOptions = new Array();
			sparkOptions['height'] = theHeight;
			sparkOptions['width'] = theWidth;
			sparkOptions['type'] = 'tristate';
			sparkOptions['disableTooltips'] = true;
			sparkOptions['disableInteraction'] = true;
			$(theElement).sparkline(theValues, sparkOptions);			
		}
		
		function jsDrawSparkline(theElement, theValues, theHeight, theWidth, theType, theTimelines) {
			var sparkOptions = new Array();
			sparkOptions['chartRangeMin'] = 0;
			sparkOptions['height'] = theHeight;
			sparkOptions['width'] = theWidth;
			sparkOptions['type'] = theType;
			sparkOptions['tooltipFormat'] = '{{offset:names}} - {{offset:values}}';
			if (theType == 'bar') {
				sparkOptions['barWidth'] = 2;
				sparkOptions['barSpacing'] = 0;
				sparkOptions['barColor'] = '#AAD';
				sparkOptions['zeroColor'] = '#CCF';
			}
		
			if (theTimelines != null) {
				var rangeNames = new Object();
				sparkOptions['tooltipValueLookups'] = rangeNames;
				rangeNames.names = new Array();
				rangeNames.values = new Array();
		
				var splitValues = theValues.split(",");
				var splitTimes = theTimelines.split(",");
				for ( var i = 0; i < splitTimes.length; i++) {
					rangeNames.names[i] = splitTimes[i];
					rangeNames.values[i] = splitValues[i];
				}
				
			}
		
			var splitValues = theValues.split(",");
			var values = new Array();
			for ( var i = 0; i < splitValues.length; i++) {
				values[i] = parseInt(splitValues[i]);
			}
		
			$(theElement).sparkline(values, sparkOptions);
		}
    </script>
    
    <title>SAIL Service Proxy</title>

  </head>

  <body>

    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

	Loading...

  </body>
</html>
