Ext.Loader.setConfig({
    enabled: true
});
Ext.Loader.setPath('Ext.ux.grid', 'From Markup Grid Example_files');

Ext.require([
    'Ext.data.*',
    'Ext.grid.*',
    'Ext.ux.grid.TransformGrid'
]);

Ext.onReady(function(){
    var btn = Ext.get("create-grid");
    btn.on("click", function(){
        var grid = Ext.create('Ext.ux.grid.TransformGrid', "the-table", {
            stripeRows: true,
            height: 130
        });
        grid.render();
    });
});

