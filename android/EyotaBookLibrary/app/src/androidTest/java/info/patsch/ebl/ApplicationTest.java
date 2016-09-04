package info.patsch.ebl;

import android.app.Application;
import android.os.Parcel;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import info.patsch.ebl.books.Book;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(JUnit4.class)
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void testTestClassParcelable(){
        Book book = createBook();


        // Obtain a Parcel object and write the parcelable object to it:
        Parcel parcel = Parcel.obtain();
        book.writeToParcel(parcel, 0);

        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        Book createdFromParcel = Book.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(book, createdFromParcel);
    }

    private Book createBook() {
        Book book = new Book();
        book.setAuthorName("Aimée Carter");
        book.setAuthorUrl("http://www.fantasticfiction.co.uk/c/aimee-carter");
        book.setComment(null);
        book.setHasImage(false);
        book.setId("-KQpFJGB7RIQtAjKfNSZ");
        book.setImage(null);
        book.setImageEncoded("iVBORw0KGgoAAAANSUhEUgAAAUUAAAH0CAIAAAAlgKjaAAAAA3NCSVQICAjb4U_gAAAgAElEQVR4\nnLS9eZAl1Xkv-H1nycybd6tb-9IrdKtpuhtoAWrA3ahpkIQAjSweaHkWQthSGGtsPArbUoRGnvAb\nb8-LLNmBn2WPFZZsS0bLszZLlmRAQixtgZp9gIbe99rvnus5Z_74bp7KulXdyI55JyoqbmWdm3ny\n5Pf79u9LfNvbbz537lyn03Ecx3EcxpgxJk1TzjkAGGOMMVprrTV9DlMlhGCAaZoyA1JKgYwmMMYQ\nUSkVp4lSCgAYY6CVlFI4EgBSrZIk0VoDgNaacy6EkFJyZIgI2gCAZhDHMf2XpgGAEIJOzhhjjGmt\nkyTJzm9obYiIiPQVrTVNpoPGGAAAAETUYOgDTbafoyiiq9izGWOUUkoIxhgHBADQBhEl43aaYYiI\nymillALDGMM0RcQ0TT3PA6273S5jTClFO-M5ruM4tB5E1FobBEREA3RCu-e0ht7OZAcBgEtBHxhj\nyBkAKKXsmjWAMYY2lm6BftME3tuOpUvQyg2ilJJzjoiYGKUUPT5guLRBjmCMoeCImKRpmqYAwDlX\nUayUAm2EEA4XiAj0LAxYqugRhtFKKcaAVsUYsw8LsmFyg47QqugMdg4tKj8Zs0Ert5P7Rn43aE7-\nd9_MVc9Aq7XL65u58nj-SN_57Vh-48p-yuPO4lFng45LFBYXiCjCMIyiKIoipVQYhvQFpZSUso-8\negQnpNYaDRCetdYKmaUb2tA0TZe2VSsAIKpNVJqmqd0Re6uGcYvnSCVxHCul7PMGACFEmqaIyDkn\neKRpaoxhjBF19u2gMUYIwTmn-ZCjm1SrVZ8ccRDCGABYbkLXFXSPBhBRMb7E7JbjGRFFtglKKdCa\nwGZZGDFKWmGPManUGKNTZZ8Q7X-P6GGJ71g8058Wz3R-ullgjG6B9t9evUf0tFHa2LMpo7XWBlEI");
        book.setImageLoc("http://ecx.images-amazon.com/images/I/51FfjnZVqjL.jpg");
        book.setBook(false);
        book.setEBook(false);
        book.setFavorite(false);
        book.setRead(false);
        book.setPfn("http://www.fantasticfiction.co.uk/c/aimee-carter/goddess-test.htm");
        book.setSeriesName("Goddess Test");
        book.setSeriesNumber("1");
        book.setTitle("The Goddess Test");
        book.setYear(2011);
        return book;
    }
}