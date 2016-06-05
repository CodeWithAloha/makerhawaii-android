package party.treesquaredcode.android.makerhawaii;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by rht on 6/4/16.
 */
public class MakerSpaceDetailActivity extends Activity {

    public static final String NAME_EXTRA_ID = "name";
    public static final String ADDRESS_EXTRA_ID = "address";
    public static final String CLASSES_EXTRA_ID = "classes";
    public static final String HOW_TO_VISIT_EXTRA_ID = "howToVisit";
    public static final String MEMBERSHIP_INFO_EXTRA_ID = "membershipInfo";
    public static final String WEBSITE_EXTRA_ID = "website";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final String name = intent.getStringExtra(NAME_EXTRA_ID);
        String address = intent.getStringExtra(ADDRESS_EXTRA_ID);
        String classes = intent.getStringExtra(CLASSES_EXTRA_ID);
        String howToVisit = intent.getStringExtra(HOW_TO_VISIT_EXTRA_ID);
        String membershipInfo = intent.getStringExtra(MEMBERSHIP_INFO_EXTRA_ID);
        final String website = intent.getStringExtra(WEBSITE_EXTRA_ID);

        setContentView(R.layout.activity__maker_space_detail_activity);

        findViewById(R.id.maker_space_detail_activity__back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView) findViewById(R.id.maker_space_detail_activity__name)).setText(name);

        if (address == null || address.isEmpty()) {
            findViewById(R.id.maker_space_detail_activity__address_container).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.maker_space_detail_activity__address)).setText(address);
        }

        if (classes == null || classes.isEmpty()) {
            findViewById(R.id.maker_space_detail_activity__classes_container).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.maker_space_detail_activity__classes)).setText(classes);
        }

        if (howToVisit == null || howToVisit.isEmpty()) {
            findViewById(R.id.maker_space_detail_activity__how_to_visit_container).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.maker_space_detail_activity__how_to_visit)).setText(howToVisit);
        }

        if (membershipInfo == null || membershipInfo.isEmpty()) {
            findViewById(R.id.maker_space_detail_activity__membership_info_container).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.maker_space_detail_activity__membership_info)).setText(membershipInfo);
        }

        if (website == null || website.isEmpty()) {
            findViewById(R.id.maker_space_detail_activity__website_container).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.maker_space_detail_activity__website)).setText(website);
            findViewById(R.id.maker_space_detail_activity__share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TITLE, name);
                    intent.putExtra(Intent.EXTRA_TEXT, website);
                    startActivity(Intent.createChooser(intent, "Share"));
                }
            });
        }
    }
}
