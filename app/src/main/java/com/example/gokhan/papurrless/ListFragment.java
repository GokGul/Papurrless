package com.example.gokhan.papurrless;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.MenuRes;
import android.support.design.widget.Snackbar;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokhan.papurrless.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matth on 11-1-2016.
 */
public class ListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static boolean premiumEnabled = false;

    public ListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ListFragment newInstance(int sectionNumber) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private ListFragment otherFrag;

    public void setOtherFrag(ListFragment frag) {
        otherFrag = frag;
    }

    //This RecyclerView holds the cards (receipts)
    RecyclerView rv;
    RVAdapter adapter;

    LinearLayoutManager llm = new LinearLayoutManager(getContext());

    //These are the receipt previews
    class ReceiptContent {
        String market;
        String dateTime;
        String date; //changes the toolbar color to match branding
        int marketColor;
        String products;
        String prices;
        String totalprice;
        boolean isFavorite, isShort;
        int receiptId;  //this ID is used to keep track of which full receipt to open when pressed

        int pricesPreviewDivider;
        int productsPreviewDivider;

        ReceiptContent(String market, String date, String products, String prices, String totalprice, boolean isFavorite, int receiptId) {
            this.market = market;
            this.dateTime = date;
            if(date.length() >= 10) {
                this.date = date.substring(0, 10);//without the time
            }
            else{
                this.date = date;
            }
            marketColor = getMarketColor(market);
            this.products = products;
            this.prices = prices;
            this.totalprice = totalprice;
            this.isFavorite = isFavorite;
            this.receiptId = receiptId;

            pricesPreviewDivider = ordinalIndexOf(prices, '\n', 4);
            productsPreviewDivider = ordinalIndexOf(products, '\n', 4);
        }

        public int getMarketColor(String market) {
            switch (market) {
                case "AH":
                    return ContextCompat.getColor(getContext(), R.color.AH);
                case "Albert Heijn":
                    return ContextCompat.getColor(getContext(), R.color.AH);
                case "Jumbo":
                    return ContextCompat.getColor(getContext(), R.color.Jumbo);
                case "Aldi":
                    return ContextCompat.getColor(getContext(), R.color.Aldi);
                case "Plus":
                    return ContextCompat.getColor(getContext(), R.color.Plus);
                case "Spar":
                    return ContextCompat.getColor(getContext(), R.color.Spar);
                case "Lidl":
                    return ContextCompat.getColor(getContext(), R.color.Lidl);
                case "Dirk":
                    return ContextCompat.getColor(getContext(), R.color.Dirk);
                case "Makro":
                    return ContextCompat.getColor(getContext(), R.color.Makro);
                case "Sligro":
                    return ContextCompat.getColor(getContext(), R.color.Sligro);
                default:
                    return ContextCompat.getColor(getContext(), R.color.Default);
            }
        }

        boolean checkId(int iD) {
            if (iD == receiptId)
                return true;
            else
                return false;
        }


        int ordinalIndexOf(String str, char c, int n) //determines the position of the nth occurrence of character c
        {
            int pos = str.indexOf(c, 0);
            while (n-- > 0 && pos != -1)
                pos = str.indexOf(c, pos + 1);
            if (pos == -1) {
                pos = str.length() - 1;
                isShort = true; //So there's no need for a restX string
            }
            return pos;
        }

        String previewProducts() {
            if (isShort)
                return products;
            else
                return products.substring(0, productsPreviewDivider);
        }

        String previewPrices() {
            if (isShort)
                return prices;
            else
                return prices.substring(0, pricesPreviewDivider);
        }

        String restProducts() {
            if (isShort)
                return "";
            else
                return products.substring(productsPreviewDivider + 1);
        }

        String restPrices() {
            if (isShort)
                return "";
            else
                return prices.substring(pricesPreviewDivider + 1);
        }
    }


    public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        // The minimum amount of items to have below your current scroll position
        // before loading more.
        private int visibleThreshold = 3;
        // The current offset index of data you have loaded
        private int currentPage = 0;
        // The total number of items in the dataset after the last load
        private int previousTotalItemCount = 0;
        // True if we are still waiting for the last set of data to load.
        private boolean loading = true;
        // Sets the starting page index
        private int startingPageIndex = 0;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
            this.mLinearLayoutManager = layoutManager;
        }

        // This happens many times a second during a scroll, so be wary of the code you place here.
        // We are given a few useful parameters to help us work out if we need to load some more data,
        // but first we check if we are waiting for the previous load to finish.
        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
            int lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
            int totalItemCount = mLinearLayoutManager.getItemCount();

            // If the total item count is zero and the previous isn't, assume the
            // list is invalidated and should be reset back to initial state
            if (totalItemCount < previousTotalItemCount) {
                this.currentPage = this.startingPageIndex;
                this.previousTotalItemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.loading = true;
                }
            }
            // If it’s still loading, we check to see if the dataset count has
            // changed, if so we conclude it has finished loading and update the current page
            // number and total item count.
            if (loading && (totalItemCount > previousTotalItemCount)) {
                loading = false;
                previousTotalItemCount = totalItemCount;
            }

            // If it isn’t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            if (!loading && (lastVisibleItemPosition + visibleThreshold) >= totalItemCount) {
                currentPage++;
                onLoadMore(currentPage, totalItemCount);
                loading = true;
            }
        }

        // Defines the process for actually loading more data based on page
        public abstract void onLoadMore(int page, int totalItemsCount);

    }

    private static List<ReceiptContent> receipts;

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ReceiptViewHolder> {

        public List<ReceiptContent> receiptsA;
        boolean isFavoriteList = false;

        RVAdapter(List<ReceiptContent> receipts) {
            this.receiptsA = receipts;
        }

        public void addReceipts(List<ReceiptContent> newReceipts) {
            receiptsA.addAll(newReceipts);
        }

        RVAdapter(List<ReceiptContent> receipts, boolean isFavoriteList) {
            this.receiptsA = receipts;
            this.isFavoriteList = isFavoriteList;
        }

        @Override
        public int getItemCount() {
            return receiptsA.size();
        }

        @Override
        public ReceiptViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.receipt_card, viewGroup, false);
            ReceiptViewHolder rvh = new ReceiptViewHolder(v);
            return rvh;
        }

        @Override
        public void onBindViewHolder(ReceiptViewHolder receiptViewHolder, int i) {
            receiptViewHolder.toolbar.setTitle(receiptsA.get(i).market.concat("\t").concat(receiptsA.get(i).date));
            receiptViewHolder.toolbar.setBackgroundColor(receiptsA.get(i).marketColor);
            receiptViewHolder.prices.setText(receiptsA.get(i).previewPrices());
            receiptViewHolder.products.setText(receiptsA.get(i).previewProducts());
            receiptViewHolder.totalprice.setText(receiptsA.get(i).totalprice);
            receiptViewHolder.favorite.setChecked(receiptsA.get(i).isFavorite);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public class ReceiptViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView cv;
            Toolbar toolbar;
            TextView products;
            TextView prices;
            TextView productsRest;
            TextView pricesRest;
            TextView totalprice;
            CheckBox favorite;
            ImageButton edit;
            ImageButton delete;
            ImageButton image;
            FullReceipt fullReceipt;

            ReceiptViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.card_view);
                toolbar = (Toolbar) cv.findViewById(R.id.toolbar);
                products = (TextView) itemView.findViewById(R.id.products);
                prices = (TextView) itemView.findViewById(R.id.prices);
                productsRest = (TextView) cv.findViewById(R.id.products_rest);
                pricesRest = (TextView) cv.findViewById(R.id.prices_rest);
                totalprice = (TextView) itemView.findViewById(R.id.totalprice);
                favorite = (CheckBox) itemView.findViewById(R.id.action_favorite);
                edit = (ImageButton) itemView.findViewById(R.id.action_edit);
                delete = (ImageButton) itemView.findViewById(R.id.action_delete);
                image = (ImageButton) itemView.findViewById(R.id.action_image);
                products.setOnClickListener(this);
                prices.setOnClickListener(this);
                productsRest.setOnClickListener(this);
                pricesRest.setOnClickListener(this);
                favorite.setOnClickListener(this);
                edit.setOnClickListener(this);
                delete.setOnClickListener(this);
                image.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                //if else because cases require constants
                if (v.getId() == products.getId() || v.getId() == prices.getId() || v.getId() == productsRest.getId() || v.getId() == pricesRest.getId()) {
                    openDetailedView();
                } else if (v.getId() == favorite.getId()) {
                    favReceipt(getAdapterPosition());
                } else if (v.getId() == edit.getId()) {
                    editReceipt();
                } else if (v.getId() == delete.getId()) {
                    deleteReceipt(getAdapterPosition());
                } else if (v.getId() == image.getId()) {
                    openImage();
                }
            }

            public void openDetailedView() {
                if (fullReceipt == null) {
                    fullReceipt = new FullReceipt(this, receiptsA.get(getAdapterPosition()).receiptId, pricesRest, productsRest);
                    fullReceipt.expand();
                } else {
                    fullReceipt.goBack();
                    fullReceipt = null;
                }

            }

            public void favReceipt(int position) {
                if (isFavoriteList)  //removes from fav list, changes checkbox in all list
                {
                    try {
                        String dateTime = receiptsA.get(position).dateTime;
                        String path = Environment.getExternalStorageDirectory().toString() +
                                "/Papurrless/scanned-data" + dateTime;

                        ArrayList<String> data = new ArrayList();
                        File file = new File(path);
                        FileInputStream fis = new FileInputStream(file);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                        String line;
                        while((line = br.readLine()) != null){
                            if(!line.equals("isFavorite")) {
                                data.add(line);
                            }
                        }
                        file.delete();
                        FileWriter fw = new FileWriter(file);
                        BufferedWriter bw = new BufferedWriter(fw);
                        for(String lines : data) {
                            bw.write(lines + "\n");
                        }
                        bw.flush();
                        bw.close();

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    favOtherList(receiptsA.get(position).receiptId, position, isFavoriteList);
                    receiptsA.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                    //update database with isFavorite = false
                } else if (receiptsA.get(position).isFavorite)  //removes from fav list (other screen), updates view
                {
                    System.out.println("remove pls");
                    try {
                        String dateTime = receiptsA.get(position).dateTime;
                        String path = Environment.getExternalStorageDirectory().toString() +
                                "/Papurrless/scanned-data" + dateTime;

                        ArrayList<String> data = new ArrayList();
                        File file = new File(path);
                        FileInputStream fis = new FileInputStream(file);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                        String line;
                        while((line = br.readLine()) != null){
                            if(!line.equals("isFavorite")) {
                                data.add(line);
                            }
                        }
                        file.delete();
                        FileWriter fw = new FileWriter(file);
                        BufferedWriter bw = new BufferedWriter(fw);
                        for(String lines : data) {
                            bw.write(lines + "\n");
                        }
                        bw.flush();
                        bw.close();

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    delOtherList(receiptsA.get(position).receiptId);
                    receipts.get(position).isFavorite = false;
                    notifyDataSetChanged();
                    //update database with isFavorite = false
                } else    //adds to fav list, updates view
                {
                    receiptsA.get(position).isFavorite = true;
                    favOtherList(receiptsA.get(position).receiptId, position, isFavoriteList);
                    try {
                        String dateTime = receiptsA.get(position).dateTime;
                        String path = Environment.getExternalStorageDirectory().toString() +
                                "/Papurrless/scanned-data" + dateTime;

                        ArrayList<String> data = new ArrayList();
                        File file = new File(path);
                        FileInputStream fis = new FileInputStream(file);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                        data.add("isFavorite");
                        String line;
                        while((line = br.readLine()) != null){
                            data.add(line);
                        }
                        file.delete();
                        FileWriter fw = new FileWriter(file);
                        BufferedWriter bw = new BufferedWriter(fw);
                        for(String lines : data) {
                            bw.write(lines + "\n");
                        }
                        bw.flush();
                        bw.close();

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    notifyDataSetChanged();
                    //update database with isFavorite = true
                }
            }

            public void editReceipt() {
                int position = getAdapterPosition();
                Intent editor = new Intent(getActivity(), EditorActivity.class);
                editor.putExtra("date", receiptsA.get(position).dateTime);
                editor.putExtra("market", receiptsA.get(position).market);
                editor.putExtra("products", receiptsA.get(position).products);
                editor.putExtra("prices", receiptsA.get(position).prices);
                editor.putExtra("totalprice", receiptsA.get(position).totalprice);
                editor.putExtra("isFavorite", receiptsA.get(position).isFavorite);
                editor.putExtra("receiptId", receiptsA.get(position).receiptId);
                startActivity(editor);
            }

            public void openImage() {
                Toast.makeText(itemView.getContext(), "OPEN IMAGE", Toast.LENGTH_SHORT).show();
                //open the original photo
            }

            public void deleteReceipt(final int position) {
                final ReceiptContent receiptBackup = receiptsA.get(position);
                final int otherListPosition = delOtherList(receiptsA.get(position).receiptId);
                String dateTime = receiptsA.get(position).dateTime;
                String path = Environment.getExternalStorageDirectory().toString() +
                        "/Papurrless/scanned-data" + dateTime;
                final ArrayList<String> receiptData = new ArrayList();
                final File backupFile = new File(path);
                try {
                    File file = new File(path);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        receiptData.add(line);
                    }
                    file.delete();
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                receiptsA.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());

                //update database

                Snackbar snackbar = Snackbar
                        .make(itemView, R.string.deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                try {
                                    backupFile.createNewFile();
                                    FileWriter fw = new FileWriter(backupFile);
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    for(String line : receiptData) {
                                        bw.write(line + "\n");
                                    }
                                    bw.close();
                                    receiptsA.add(position, receiptBackup);
                                    notifyItemInserted(position);
                                    rv.scrollToPosition(position);
                                    if (otherListPosition != -1)
                                        undoOtherList(otherListPosition, receiptBackup, backupFile);
                                    //update database again
                                    System.out.println(backupFile.getPath());
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                snackbar.show();
            }
        }

        public class FullReceipt {
            int receiptPos;
            CardView cv;
            TextView productsRest;
            TextView pricesRest;
            TextView totalPrice;
            ImageView gradientCutoff;
            Toolbar bottomToolbar;
            ReceiptViewHolder receiptViewHolder;

            FullReceipt(ReceiptViewHolder rvh, int receiptId, TextView priRest, TextView proRest) {
                cv = rvh.cv;
                receiptViewHolder = rvh;
                bottomToolbar = (Toolbar) cv.findViewById(R.id.toolbar_bottom);
                gradientCutoff = (ImageView) cv.findViewById(R.id.imageView);
                productsRest = proRest;
                pricesRest = priRest;
                totalPrice = (TextView) cv.findViewById(R.id.totalprice);
                receiptPos = findReceipt(receiptId);
                productsRest.setText(getRestProducts());
                pricesRest.setText(getRestPrices());
            }

            int findReceipt(int receiptId) {
                int position = 0;
                for (int i = 0; i < receiptsA.size(); i++) {
                    if (receiptsA.get(i).checkId(receiptId))
                        position = i;
                }
                return position;
            }

            String getRestProducts() {
                return receiptsA.get(receiptPos).restProducts();
            }

            String getRestPrices() {
                return receiptsA.get(receiptPos).restPrices();
            }

            public void expand() {
                //set text views as visible and wrapping
                ViewGroup.LayoutParams paramsProducts = productsRest.getLayoutParams();
                paramsProducts.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                productsRest.setLayoutParams(paramsProducts);
                ViewGroup.LayoutParams paramsPrices = pricesRest.getLayoutParams();
                paramsPrices.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                pricesRest.setLayoutParams(paramsPrices);
                productsRest.setVisibility(View.VISIBLE);
                pricesRest.setVisibility(View.VISIBLE);
                //remove bottom bar
                bottomToolbar.setVisibility(View.GONE);
                //remove gradient cutoff
                gradientCutoff.setVisibility(View.GONE);
                //put total price below prices
                PercentRelativeLayout.LayoutParams paramsTotalPrice = (PercentRelativeLayout.LayoutParams) totalPrice.getLayoutParams();
                paramsTotalPrice.addRule(PercentRelativeLayout.ALIGN_PARENT_BOTTOM);
                //enable back button
            }

            public void goBack() {
                productsRest.setText("");
                pricesRest.setText("");
                ViewGroup.LayoutParams paramsProducts = productsRest.getLayoutParams();
                paramsProducts.height = 0;
                productsRest.setLayoutParams(paramsProducts);
                ViewGroup.LayoutParams paramsPrices = pricesRest.getLayoutParams();
                paramsPrices.height = 0;
                pricesRest.setLayoutParams(paramsPrices);
                productsRest.setVisibility(View.GONE);
                pricesRest.setVisibility(View.GONE);
                bottomToolbar.setVisibility(View.VISIBLE);
                gradientCutoff.setVisibility(View.VISIBLE);
                PercentRelativeLayout.LayoutParams paramsTotalPrice = (PercentRelativeLayout.LayoutParams) totalPrice.getLayoutParams();
                paramsTotalPrice.addRule(PercentRelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                rv.scrollToPosition(receiptViewHolder.getAdapterPosition());
            }
        }
    }

    public void favOtherList(int receiptId, int currentPosition, boolean isFavoriteList) {
        if (isFavoriteList) {
            int position = -1;
            RVAdapter otherAdapter = otherFrag.adapter;
            for (int i = 0; i < otherAdapter.receiptsA.size(); i++) {
                if (otherAdapter.receiptsA.get(i).checkId(receiptId))
                    position = i;
            }
            if (position > -1) {
                otherAdapter.receiptsA.get(position).isFavorite = false;
                otherAdapter.notifyDataSetChanged();
            }
        } else {
            RVAdapter otherAdapter = otherFrag.adapter;
            otherAdapter.receiptsA.add(0, adapter.receiptsA.get(currentPosition));
            otherAdapter.notifyDataSetChanged();
        }
    }

    public int delOtherList(int receiptId) {
        int position = -1;
        RVAdapter otherAdapter = otherFrag.adapter;
        for (int i = 0; i < otherAdapter.receiptsA.size(); i++) {
            if (otherAdapter.receiptsA.get(i).checkId(receiptId))
                position = i;
        }
        if (position > -1) {
            otherAdapter.receiptsA.remove(position);
            otherAdapter.notifyItemRemoved(position);
            otherAdapter.notifyItemRangeChanged(position, adapter.getItemCount());
        }
        return position;
    }

    public void undoOtherList(int position, ReceiptContent receiptBackup, File backupFile) {
        RVAdapter otherAdapter = otherFrag.adapter;
        try {
            backupFile.createNewFile();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        otherAdapter.receiptsA.add(position, receiptBackup);
        otherAdapter.notifyDataSetChanged();
    }

    //This fragment holds the favourite cards
    public static class FavFragment extends ListFragment {
        public static ListFragment newInstance(int sectionNumber, boolean premium) {
            premiumEnabled = premium;
            ListFragment fragment = new FavFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void initializeData() {
            final MainActivity mainActivity = (MainActivity)getActivity();
            receipts = new ArrayList<>();
            String path = Environment.getExternalStorageDirectory().toString() + "/Papurrless";
            File f = new File(path);
            File[] files = f.listFiles();

            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().substring(0, 12).equals("scanned-data")) {
                        List<String> receiptData = new ArrayList();
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(files[i]));
                            String line;
                            while ((line = br.readLine()) != null) {
                                receiptData.add(line);
                            }
                            mainActivity.processReceipt(receiptData, true, files[i].getName().
                                    substring(12, files[i].getName().length()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        public void addReceipt(ReceiptContent newReceipt) {
            adapter.receiptsA.add(0, newReceipt);
            adapter.notifyDataSetChanged();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            rv = (RecyclerView) rootView.findViewById(R.id.cardList);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(llm);
            initializeData();
            adapter = new RVAdapter(receipts, true);
            rv.setAdapter(adapter);
            rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    // fetch data asynchronously here
                    loadMore(page);
                }
            });
            return rootView;
        }

        public void loadMore(int offset) {
            int curSize = adapter.getItemCount();
            List<ReceiptContent> moreReceipts;
            moreReceipts = new ArrayList<>();
            adapter.addReceipts(moreReceipts);
            adapter.notifyItemRangeInserted(curSize, receipts.size() - 1);
        }
    }



    //This fragment holds all cards
    public static class AllFragment extends ListFragment {

        public static ListFragment newInstance(int sectionNumber, boolean premium) {
            premiumEnabled = premium;
            ListFragment fragment = new AllFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void initializeData() {
            final MainActivity mainActivity = (MainActivity)getActivity();
            receipts = new ArrayList<>();

            ParseUser user = ParseUser.getCurrentUser();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
            query.whereEqualTo("User", user);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size() > 0) {
                        for (int i = 0; i < objects.size(); i++) {
                            String store = objects.get(i).get("store").toString();
                            String date = objects.get(i).get("date").toString();
                            String products = objects.get(i).get("products").toString();
                            String prices = objects.get(i).get("prices").toString();
                            String subtotaal = objects.get(i).get("subtotaal").toString();

                            System.out.println(store);

                            receipts.add(new ReceiptContent(store, date, products, prices, subtotaal, false, 11));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        String path = Environment.getExternalStorageDirectory().toString() + "/Papurrless";
                        File f = new File(path);
                        File[] files = f.listFiles();

                        if (files.length > 0) {
                            for (int i = 0; i < files.length; i++) {
                                if (files[i].getName().substring(0, 12).equals("scanned-data")) {
                                    List<String> receiptData = new ArrayList();
                                    try {
                                        BufferedReader br = new BufferedReader(new FileReader(files[i]));
                                        String line;
                                        while ((line = br.readLine()) != null) {
                                            receiptData.add(line);
                                        }
                                        mainActivity.processReceipt(receiptData, true, files[i].getName().
                                                substring(12, files[i].getName().length()));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), "No local receipt files found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_all, container, false);
            rv = (RecyclerView) rootView.findViewById(R.id.cardList);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(llm);
            initializeData();
            adapter = new RVAdapter(receipts);
            rv.setAdapter(adapter);
            rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    // fetch data asynchronously here
                    loadMore(page);
                }
            });
            return rootView;
        }

        public void addReceipt(ReceiptContent newReceipt) {

            adapter.receiptsA.add(0, newReceipt);
            adapter.notifyDataSetChanged();
        }

        public void loadMore(int offset) {
            int curSize = adapter.getItemCount();
            List<ReceiptContent> moreReceipts;
            moreReceipts = new ArrayList<>();
            adapter.addReceipts(moreReceipts);
            adapter.notifyItemRangeInserted(curSize, receipts.size() - 1);
        }
    }
}