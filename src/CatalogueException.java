final class CatalogueException extends Exception {

    CatalogueException(int lineNumber, String problem) {
        super("Line " + lineNumber + ": " + problem);
    }
}
