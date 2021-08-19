/**
 * Send the Label XML to the Printer via the Dymo JS Framework
 * @param xml Label XML
 */
function makeLabel(xml) {
    try {
        var label = dymo.label.framework.openLabelXml(xml);

        // select printer to print on
        // for simplicity sake just use the first LabelWriter printer
        var printers = dymo.label.framework.getPrinters();
        if (printers.length == 0)
            throw "No DYMO printers are installed. Install DYMO printers.";

        var printerName = "";
        for (var i = 0; i < printers.length; ++i) {
            var printer = printers[i];
            if (printer.printerType == "LabelWriterPrinter") {
                printerName = printer.name;
                break;
            }
        }

        if (printerName == "")
            throw "No LabelWriter printers found. Install LabelWriter printer";

        // finally print the label
        label.print(printerName);
    }
    catch (e) {
        swal("Oops...", e.message || e, "error");
    }
}