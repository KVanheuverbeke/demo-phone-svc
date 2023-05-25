package be.demo.normalizephone.beans;

public interface Views {

    interface Limited {
    }

    interface ExtendedLimited extends Limited {
    }

    interface Internal extends ExtendedLimited {
    }

}