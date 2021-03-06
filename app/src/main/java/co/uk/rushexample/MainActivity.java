package co.uk.rushexample;

import android.os.*;
import android.view.*;

import androidx.appcompat.app.*;
import androidx.drawerlayout.widget.*;
import androidx.fragment.app.*;

public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

        /**
         * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
         */
        private NavigationDrawerFragment mNavigationDrawerFragment;

        /**
         * Used to store the last screen title. For use in {@link //#restoreActionBar()}.
         */
        private CharSequence mTitle;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }

        @Override
        public void onNavigationDrawerItemSelected(int position) {

            Fragment fragment;

            switch (position) {
                case 0:
                    fragment = new RushOrmFragment();
                    break;
                default:
                    throw new IndexOutOfBoundsException();
            }

            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }

        public void onSectionAttached(int number) {
            switch (number) {
                case 1:
                    mTitle = getString(R.string.title_section1);
                    break;
            }
        }

//        public void restoreActionBar() {
//            ActionBar actionBar = getSupportActionBar();
//            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//            actionBar.setDisplayShowTitleEnabled(true);
//            actionBar.setTitle(mTitle);
//        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            if (!mNavigationDrawerFragment.isDrawerOpen()) {
                // Only show items in the action bar relevant to this screen
                // if the drawer is not showing. Otherwise, let the drawer
                // decide what to show in the action bar.
                //getMenuInflater().inflate(R.menu.my, menu);
//                restoreActionBar();
                return true;
            }
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }
