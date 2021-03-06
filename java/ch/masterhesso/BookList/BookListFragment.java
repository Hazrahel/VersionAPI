package ch.masterhesso.BookList;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ch.masterhesso.BookList.adapters.BookDataAdapter;
import ch.masterhesso.BookList.data.BookData;
import ch.masterhesso.BookList.tasks.GoogleBooksAPIIconTask;
import ch.masterhesso.BookList.tasks.GoogleBooksWebAPITask;

/**
 * Find/display book list in order of ratings for authors/categories given in search
 * 
 * @author Cédric Droguet
 *
 */
public class BookListFragment extends Fragment {

	private ArrayList<BookData> books;
	private ListView bookList;
	private LayoutInflater layoutInflator;
	private Button booksButton;
    private EditText searchParams;
    private InputMethodManager inMgr;
    private GoogleBooksAPIIconTask imgFetcher;
    private Spinner patternSpinner;
    private Spinner languageSpinner;

    private final String debugTag = "BookListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment, container, false);

        if(savedInstanceState != null)
        {
            Log.d(debugTag, "SavedInstanceState ! = null");
            this.books = savedInstanceState.getParcelableArrayList("DATA");
            if(this.books != null) {
                if (imgFetcher == null) this.imgFetcher = new GoogleBooksAPIIconTask(getActivity());

                this.bookList = (ListView) view.findViewById(R.id.book_list);

                bookList.setAdapter(new BookDataAdapter(this, this.imgFetcher, inflater, this.books));
            }
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        
        this.inMgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        this.bookList = (ListView) getView().findViewById(R.id.book_list);
        this.imgFetcher = new GoogleBooksAPIIconTask(getActivity());
        this.layoutInflator = LayoutInflater.from(getActivity());
        this.booksButton = (Button)getView().findViewById(R.id.book_button);
        this.searchParams = (EditText)getView().findViewById(R.id.search_query);
        searchParams.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    booksButton.performClick();
                    return true;
                }
                return false;
            }
        });

        this.patternSpinner = (Spinner)getView().findViewById(R.id.pattern_chooser);
        this.languageSpinner = (Spinner)getView().findViewById(R.id.language_chooser);
        
        this.booksButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				inMgr.hideSoftInputFromWindow(booksButton.getWindowToken(), 0);
		        GoogleBooksWebAPITask bookTask = new GoogleBooksWebAPITask(BookListFragment.this);
		        try {
                    String searchCriteria = "";
                    String searchLanguage = "";
                    switch(patternSpinner.getSelectedItemPosition())
                    {
                        case 1:
                            searchCriteria = "inauthor:";
                            break;
                        case 2:
                            searchCriteria = "intitle:";
                            break;
                        default:
                            searchCriteria = "";
                    }

                    switch(languageSpinner.getSelectedItemPosition())
                    {
                        case 1:
                            searchLanguage = "en";
                            break;
                        case 2:
                            searchLanguage = "fr";
                            break;
                        case 3:
                            searchLanguage = "de";
                            break;
                        default:
                            searchLanguage = "";
                    }

                    bookTask.execute(searchParams.getText().toString(), searchCriteria, searchLanguage);
		        }
		        catch (Exception e)
		        {
		            bookTask.cancel(true);
		            alert (getResources().getString(R.string.no_books));
		        }
		        
				
			}
		});
    }

    /**
     * Handy dandy alerter.
     * @param msg the message to toast.
     */
    public void alert (String msg)
    {
        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Save any fetched book data for orientation changes.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("DATA", books);
    }

    /**
     * Bundle to hold refs to row items views.
     *
     */
    public static class MyViewHolder {
        public TextView bookTitle, authorName, bookDescription;
        public RatingBar ratingBar;
        public ImageView icon;
        public BookData book;
    }

	public void setBooks(ArrayList<BookData> books) {
		this.books = books;
		this.bookList.setAdapter(new BookDataAdapter(this, this.imgFetcher, this.layoutInflator, this.books));
	}

    
}