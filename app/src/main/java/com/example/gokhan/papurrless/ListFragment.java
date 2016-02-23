package com.example.gokhan.papurrless;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokhan.papurrless.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matth on 11-1-2016.
 */
public class ListFragment extends Fragment{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

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

    public void setOtherFrag(ListFragment frag)
    {
        otherFrag = frag;
    }

    //This RecyclerView holds the cards (receipts)
    RecyclerView rv;
    RVAdapter adapter;

    LinearLayoutManager llm = new LinearLayoutManager(getContext());

    //These are the receipt previews
    class ReceiptContent
    {
        String market;
        String date; //changes the toolbar color to match branding
        int marketColor;
        String products;
        String prices;
        String totalprice;
        boolean isFavorite;
        int receiptId;  //this ID is used to keep track of which full receipt to open when pressed

        ReceiptContent(String market, String date, String products, String prices, String totalprice, boolean isFavorite, int receiptId)
        {
            this.market = market;
            this.date = date;
            marketColor = getMarketColor(market);
            this.products = products;
            this.prices = prices;
            this.totalprice = totalprice;
            this.isFavorite = isFavorite;
            this.receiptId = receiptId;
        }

        int getMarketColor(String market)
        {
            switch(market)
            {
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

        boolean checkId(int iD)
        {
            if(iD==receiptId)
                return true;
            else
                return false;
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

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ReceiptViewHolder>{

        public List<ReceiptContent> receipts;
        boolean isFavoriteList = false;

        RVAdapter(List<ReceiptContent> receipts)
        {
            this.receipts = receipts;
        }

        public void addReceipts(List<ReceiptContent> newReceipts)
        {
            receipts.addAll(newReceipts);
        }

        RVAdapter(List<ReceiptContent> receipts, boolean isFavoriteList)
        {
            this.receipts = receipts;
            this.isFavoriteList = isFavoriteList;
        }

        @Override
        public int getItemCount() {
            return receipts.size();
        }

        @Override
        public ReceiptViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.receipt_card, viewGroup, false);
            ReceiptViewHolder rvh = new ReceiptViewHolder(v);
            return rvh;
        }

        @Override
        public void onBindViewHolder(ReceiptViewHolder receiptViewHolder, int i)
        {
            receiptViewHolder.toolbar.setTitle(receipts.get(i).market.concat("\t").concat(receipts.get(i).date));
            receiptViewHolder.toolbar.setBackgroundColor(receipts.get(i).marketColor);
            receiptViewHolder.prices.setText(receipts.get(i).prices);
            receiptViewHolder.products.setText(receipts.get(i).products);
            receiptViewHolder.totalprice.setText(receipts.get(i).totalprice);
            receiptViewHolder.favorite.setChecked(receipts.get(i).isFavorite);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public class ReceiptViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            CardView cv;
            Toolbar toolbar;
            TextView products;
            TextView prices;
            TextView totalprice;
            CheckBox favorite;
            ImageButton edit;
            ImageButton delete;
            FullReceipt fullReceipt;

            ReceiptViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.card_view);
                toolbar = (Toolbar)cv.findViewById(R.id.toolbar);
                products = (TextView)itemView.findViewById(R.id.products);
                prices = (TextView)itemView.findViewById(R.id.prices);
                totalprice = (TextView)itemView.findViewById(R.id.totalprice);
                favorite = (CheckBox)itemView.findViewById(R.id.action_favorite);
                edit = (ImageButton)itemView.findViewById(R.id.action_edit);
                delete = (ImageButton)itemView.findViewById(R.id.action_delete);
                itemView.setOnClickListener(this);
                favorite.setOnClickListener(this);
                edit.setOnClickListener(this);
                delete.setOnClickListener(this);
            }

            @Override
            public void onClick(View v)
            {
                //if else because cases require constants
                if (v.getId() == itemView.getId()){
                    openDetailedView();
                } else if (v.getId() == favorite.getId()){
                    favReceipt(getAdapterPosition());
                } else if (v.getId() == edit.getId()){
                    editReceipt();
                } else if (v.getId() == delete.getId()){
                    deleteReceipt(getAdapterPosition());
                }
            }

            public void openDetailedView()
            {
                if(fullReceipt==null)
                {
                    fullReceipt = new FullReceipt(this, receipts.get(getAdapterPosition()).receiptId);
                    fullReceipt.expand();
                }
                else
                {
                    fullReceipt.goBack();
                    fullReceipt = null;
                }

            }

            public void favReceipt(int position)
            {
                Toast.makeText(itemView.getContext(), "FAVORITE", Toast.LENGTH_SHORT).show();
                if(isFavoriteList)  //removes from fav list, changes checkbox in all list
                {
                    favOtherList(receipts.get(position).receiptId, position, isFavoriteList);
                    receipts.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                    //update database with isFavorite = false
                }
                else if(receipts.get(position).isFavorite)  //removes from fav list (other screen), updates view
                {
                    delOtherList(receipts.get(position).receiptId);
                    receipts.get(position).isFavorite=false;
                    notifyDataSetChanged();
                    //update database with isFavorite = false
                }
                else    //adds to fav list, updates view
                {
                    receipts.get(position).isFavorite=true;
                    favOtherList(receipts.get(position).receiptId, position, isFavoriteList);
                    notifyDataSetChanged();
                    //update database with isFavorite = true
                }
            }

            public void editReceipt()
            {
                Toast.makeText(itemView.getContext(), "EDIT", Toast.LENGTH_SHORT).show();
                //start edit activity
            }

            public void deleteReceipt(final int position)
            {
                final ReceiptContent receiptBackup = receipts.get(position);

                final int otherListPosition = delOtherList(receipts.get(position).receiptId);
                receipts.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());

                //update database

                Snackbar snackbar = Snackbar
                        .make(itemView, R.string.deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                receipts.add(position, receiptBackup);
                                notifyItemInserted(position);
                                rv.scrollToPosition(position);
                                if(otherListPosition!=-1)
                                    undoOtherList(otherListPosition, receiptBackup);
                                //update database again
                            }
                        });
                snackbar.show();
            }
        }

        public class FullReceipt
        {
            int receiptId;
            CardView cv;
            TextView productsRest;
            TextView pricesRest;
            TextView totalPrice;
            ImageView gradientCutoff;
            Toolbar bottomToolbar;
            ReceiptViewHolder receiptViewHolder;

            FullReceipt(ReceiptViewHolder rvh, int receiptId) {
                cv = rvh.cv;
                receiptViewHolder = rvh;
                productsRest = (TextView)cv.findViewById(R.id.products_rest);
                pricesRest = (TextView)cv.findViewById(R.id.prices_rest);
                bottomToolbar = (Toolbar)cv.findViewById(R.id.toolbar_bottom);
                gradientCutoff = (ImageView)cv.findViewById(R.id.imageView);
                totalPrice = (TextView)cv.findViewById(R.id.totalprice);
                productsRest.setText(getRestProducts());
                pricesRest.setText(getRestPrices());
            }

            String getRestProducts()
            {
                String restProducts = getString(R.string.products_rest); //REPLACE WITH DATABASE QUERY
                return restProducts;
            }

            String getRestPrices()
            {
                String restPrices = getString(R.string.prices_rest); //REPLACE WITH DATABASE QUERY
                return restPrices;
            }

            public void expand()
            {
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
                PercentRelativeLayout.LayoutParams paramsTotalPrice = (PercentRelativeLayout.LayoutParams)totalPrice.getLayoutParams();
                paramsTotalPrice.addRule(PercentRelativeLayout.ALIGN_PARENT_BOTTOM);
                //enable back button
            }

            public void goBack()
            {
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
                PercentRelativeLayout.LayoutParams paramsTotalPrice = (PercentRelativeLayout.LayoutParams)totalPrice.getLayoutParams();
                paramsTotalPrice.addRule(PercentRelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                rv.scrollToPosition(receiptViewHolder.getAdapterPosition());
            }
        }
    }

    public void favOtherList(int receiptId, int currentPosition, boolean isFavoriteList)
    {
        if(isFavoriteList) {
            int position = -1;
            RVAdapter otherAdapter = otherFrag.adapter;
            for(int i = 0; i < otherAdapter.receipts.size(); i++)
            {
                if(otherAdapter.receipts.get(i).checkId(receiptId))
                    position = i;
            }
            if(position>-1)
            {
                otherAdapter.receipts.get(position).isFavorite=false;
                otherAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            RVAdapter otherAdapter = otherFrag.adapter;
            otherAdapter.receipts.add(0, adapter.receipts.get(currentPosition));
            otherAdapter.notifyDataSetChanged();
        }
    }

    public int delOtherList(int receiptId)
    {
        int position = -1;
        RVAdapter otherAdapter = otherFrag.adapter;
        for(int i = 0; i < otherAdapter.receipts.size(); i++)
        {
            if(otherAdapter.receipts.get(i).checkId(receiptId))
                position = i;
        }
        if(position>-1)
        {
            otherAdapter.receipts.remove(position);
            otherAdapter.notifyItemRemoved(position);
            otherAdapter.notifyItemRangeChanged(position, adapter.getItemCount());
        }
        return position;
    }

    public void undoOtherList(int position, ReceiptContent receiptBackup)
    {
        RVAdapter otherAdapter = otherFrag.adapter;
        otherAdapter.receipts.add(position, receiptBackup);
        otherAdapter.notifyDataSetChanged();
    }

    //This fragment holds the favourite cards
    public static class FavFragment extends ListFragment {
        public static ListFragment newInstance(int sectionNumber) {
            ListFragment fragment = new FavFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void initializeData()
        {
            receipts = new ArrayList<>();
            receipts.add(new ReceiptContent("AH", "01-01-2021", "2x COCA-COLA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€100,00", true, 22));
            receipts.add(new ReceiptContent("Jumbo", "18-06-2011", "2x FANTA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€100,00", true, 50));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            rv = (RecyclerView)rootView.findViewById(R.id.cardList);
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
            //add a certain number of receipts (use page to determine the range, or I guess the key would be better
            if(offset<13) //this isn't relevant, just a useful limiter
            {
                moreReceipts.add(new ReceiptContent("DerpMarkt", "01-01-2009", "2x COCA-COLA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€100,00", true, 12));
            }
            //normally you'd be pulling from the database at this point, note to only pull favorites
            adapter.addReceipts(moreReceipts);
            adapter.notifyItemRangeInserted(curSize, receipts.size() - 1);
        }
    }


    //This fragment holds all cards
    public static class AllFragment extends ListFragment {

        public static ListFragment newInstance(int sectionNumber) {
            ListFragment fragment = new AllFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void initializeData()
        {
            receipts = new ArrayList<>();
            receipts.add(new ReceiptContent("AH", "01-01-2021", "2x COCA-COLA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€100,00", true, 22));
            receipts.add(new ReceiptContent("Jumbo", "18-06-2011", "2x FANTA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€100,00", true, 50));
            receipts.add(new ReceiptContent("Spar", "01-01-2011", "2x COCA-COLA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€90,00", false, 100));
            receipts.add(new ReceiptContent("Dirk", "01-01-2010", "2x COCA-COLA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€80,00", false, 33));
            receipts.add(new ReceiptContent("DerpMarkt", "01-01-2009", "2x COCA-COLA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€100,00", false, 12));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_all, container, false);
            rv = (RecyclerView)rootView.findViewById(R.id.cardList);
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

        public void loadMore(int offset) {
            int curSize = adapter.getItemCount();
            List<ReceiptContent> moreReceipts;
            moreReceipts = new ArrayList<>();
            //add a certain number of receipts (use page to determine the range, or I guess the key would be better
            if(offset<13) //this isn't relevant, just a useful limiter
            {
                moreReceipts.add(new ReceiptContent("DerpMarkt", "01-01-2009", "2x COCA-COLA\n2x APPELS\n1x DURR\n1x CUTOFF", "€2,33\n€3,75\n€11,22\n€13,77", "€100,00", true, 12));
            }
            //normally you'd be pulling from the database at this point
            adapter.addReceipts(moreReceipts);
            adapter.notifyItemRangeInserted(curSize, receipts.size() - 1);
        }
    }
}