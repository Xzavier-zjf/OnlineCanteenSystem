package com.canteen.merchant.ui.panel;

import com.canteen.merchant.dto.MerchantUser;
import com.canteen.merchant.model.Product;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class ProductEditDialog extends JDialog {

    public ProductEditDialog(JFrame parent, MerchantUser currentUser) {
        super(parent, true);
    }

    public ProductEditDialog(JFrame parent, Product product, MerchantUser currentUser) {
        super(parent, true);
    }

    public Product getUpdatedProduct() {
        return null;
    }

    public boolean isConfirmed() {
        return false;
    }
}