package com.android.serviceproviderapplication.Helper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.serviceproviderapplication.Common.Common;
import com.android.serviceproviderapplication.R;



public class BottomSheetBillCalculate extends BottomSheetDialogFragment {
    int mins,sec;
    public TextView time,price;
    public static BottomSheetBillCalculate newInstance(int mins,int sec)
    {
        BottomSheetBillCalculate b=new BottomSheetBillCalculate();
        Bundle args=new Bundle();
        args.putInt("mints",mins);
        args.putInt("sec",sec);
        b.setArguments(args);
        return b;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mins=getArguments().getInt("mints");
        sec=getArguments().getInt("sec");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.bottoom_sheet_bil_calculate,container,false);
        time=view.findViewById(R.id.timeatwork);
        price=view.findViewById(R.id.price);
        getPrice(mins,sec);
        time.setText(mins+"m"+sec+"s");
        return view;
    }

    public void getPrice(int mins,int sec) {
        String ans=Double.toString(Common.getPrice(mins,sec));
        price.setText(ans);
    }
}
