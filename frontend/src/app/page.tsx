import RootLayout from "@/app/layout";

export default function Home() {
  return (
    <RootLayout>
      <div className="flex flex-col flex-1 items-center justify-center bg-zinc-50 font-sans dark:bg-black">
        <h1 className="text-3xl font-bold">ERP Platform</h1>
      </div>
    </RootLayout>
  );
}
